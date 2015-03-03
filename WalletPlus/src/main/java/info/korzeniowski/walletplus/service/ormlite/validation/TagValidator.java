package info.korzeniowski.walletplus.service.ormlite.validation;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;

import static com.google.common.base.Preconditions.checkNotNull;

public class TagValidator implements Validator<Tag> {
    private final TagService tagService;

    public TagValidator(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    public void validateInsert(Tag tag) {
        checkNotNull(tag);
        validateIfNameIsNotNullOrEmpty(tag);
        validateIfIdIsUnique(tag);
    }

    @Override
    public void validateDelete(Long id) {

    }

    @Override
    public void validateUpdate(Tag newTag) {
        checkNotNull(newTag);
        Tag oldTag = tagService.findById(newTag.getId());
        validateIfNameIsNotNullOrEmpty(newTag);
        validateIfNewIdIsUnique(newTag, oldTag);
    }

    /**
     * ****************************
     * Unit validations
     * *****************************
     */

    private void validateIfNameIsNotNullOrEmpty(Tag tag) {
        if (Strings.isNullOrEmpty(tag.getName())) {
            throw new EntityPropertyCannotBeNullOrEmptyException(Tag.class.getSimpleName(), "Name");
        }
    }

    private void validateIfIdIsUnique(Tag tag) {
        if (tag.getId() != null && tagService.findById(tag.getId()) != null) {
            throw new EntityAlreadyExistsException(Tag.class.getSimpleName(), tag.getId());
        }
    }

    private void validateIfNewIdIsUnique(Tag newValue, Tag toUpdate) {
        if (!Objects.equal(newValue.getId(), toUpdate.getId())) {
            validateIfIdIsUnique(newValue);
        }
    }
}
