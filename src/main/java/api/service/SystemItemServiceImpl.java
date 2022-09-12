package api.service;

import api.domain.SystemItem;
import api.domain.SystemItem.Type;
import api.exceptions.SystemItemDuplicateException;
import api.exceptions.SystemItemParentNotFoundException;
import api.exceptions.SystemItemWrongTypeException;
import api.repository.SystemItemImportRepository;
import api.repository.SystemItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor
@Service
public class SystemItemServiceImpl implements SystemItemService {

  private final SystemItemRepository itemRepository;
  private final SystemItemImportRepository importsRepository;

  @Transactional(readOnly = true)
  @Override
  public Optional<SystemItem> get(String id) {
    return itemRepository.findById(id);
  }

  @Transactional
  @Override
  public void delete(String id) {
    var foldersIds = new HashSet<>(getFoldersIds(id));
    var descendants = itemRepository.findAllDescendants(id);

    itemRepository.deleteById(id);
    itemRepository.deleteAll(descendants);

    importsRepository.delete(id);
    updateFolders(foldersIds, null);
  }

  @Transactional
  @Override
  public void doImport(List<SystemItem> items, ZonedDateTime date) {
    var ids = new HashSet<String>();
    var foldersIds = new HashSet<String>();

    IntStream.range(0, 2).forEach((i) -> items.forEach((item) -> {
      if (ids.contains(item.getId()) && i == 0) {
        throw new SystemItemDuplicateException(String.format("Same id %s in request body", item.getId()));
      }

      if (ids.contains(item.getId()) && i == 1) {
        return;
      }

      try {
        updateOrCreate(item, foldersIds);
        ids.add(item.getId());
      } catch (Exception e) {
        if (e instanceof SystemItemParentNotFoundException && i == 0) {
          return;
        }
        throw e;
      }
    }));

    updateFolders(foldersIds, date);
  }

  private void updateFolders(Set<String> foldersIds, ZonedDateTime date) {
    for (var id : foldersIds) {
      itemRepository.findById(id).ifPresent((folder) -> {
        var size = itemRepository.calcSize(folder.getId());
        folder.setSize(size);
        if (date != null) {
          folder.setDate(date);
        }
        itemRepository.save(folder);
      });
    }
  }

  private List<String> getFoldersIds(String itemId) {
    var folders = itemRepository.findParentFolders(itemId);
    return folders.stream().map(SystemItem::getId).collect(Collectors.toList());
  }

  private void updateOrCreate(SystemItem newItem, Set<String> foldersIds) {
    var parentId = newItem.getParentId();
    if (parentId != null) {
      itemRepository.findById(parentId).ifPresentOrElse((parent) -> {
        if (parent.getType() != Type.FOLDER) {
          throw new SystemItemWrongTypeException(String.format("Item id=%s has wrong parent type!", newItem.getId()));
        }

        if (Objects.equals(newItem.getId(), parentId)) {
          throw new SystemItemDuplicateException(String.format("Parent with the same id=%s", parentId));
        }
      }, () -> {
        throw new SystemItemParentNotFoundException(String.format("Parent id=%s not found!", parentId));
      });
    }

    itemRepository.findById(newItem.getId()).ifPresentOrElse((oldItem) -> {
      if (oldItem.getType() != newItem.getType()) {
        throw new SystemItemWrongTypeException("Types have to be equal");
      }

      var isMoving = !Objects.equals(oldItem.getParentId(), newItem.getParentId());
      if (isMoving) {
        // get old tree
        foldersIds.addAll(getFoldersIds(newItem.getId()));

        importsRepository.moveTree(newItem.getId(), newItem.getParentId());
        // get newly inserted tree
        foldersIds.addAll(getFoldersIds(newItem.getId()));
      }

      var isSizeChanged = !Objects.equals(newItem.getSize(), oldItem.getSize());
      if (isSizeChanged) {
        oldItem.setSize(newItem.getSize());
        foldersIds.addAll(getFoldersIds(newItem.getId()));
      }

      oldItem.setUrl(newItem.getUrl());
      oldItem.setDate(newItem.getDate());
      oldItem.setParentId(newItem.getParentId());

      // save
      itemRepository.save(oldItem);
      // update closure table
    }, () -> {
      var saved = itemRepository.save(newItem);
      importsRepository.insertNode(saved.getParentId(), saved.getId());
      // add new ancestor
      foldersIds.addAll(getFoldersIds(newItem.getId()));
    });
  }

}
