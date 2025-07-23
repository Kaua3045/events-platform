package com.kaua.events.platform.domain.eventmanagement;

import com.kaua.events.platform.domain.AggregateRoot;
import com.kaua.events.platform.domain.organizations.OrganizationID;
import com.kaua.events.platform.domain.utils.Generated;
import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.domain.utils.ULID;
import com.kaua.events.platform.domain.validation.ValidationHandler;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class Event extends AggregateRoot<EventID> {

    private OrganizationID organizationId;
    private String title;
    private String description;
    private EventStatus status;
    private EventType type;
    private Address address;
    private String imageUrl;
    private String categoryId; // TODO in future change string to CategoryID and create categories
    private Instant startAt;
    private Instant finishAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private Event(
            final EventID aEventID,
            final long aVersion,
            final OrganizationID aOrganizationId,
            final String aTitle,
            final String aDescription,
            final EventStatus aStatus,
            final EventType aType,
            final Address aAddress,
            final String aImageUrl,
            final String aCategoryId,
            final Instant aStartAt,
            final Instant aFinishAt,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aDeletedAt
    ) {
        super(aEventID, aVersion);
        setOrganizationId(aOrganizationId);
        setTitle(aTitle);
        setDescription(aDescription);
        setStatus(aStatus);
        setType(aType);
        setAddress(aAddress);
        setImageUrl(aImageUrl);
        setCategoryId(aCategoryId);
        setStartAt(aStartAt);
        setFinishAt(aFinishAt);
        setCreatedAt(aCreatedAt);
        setUpdatedAt(aUpdatedAt);
        setDeletedAt(aDeletedAt);
    }

    public static Event newEvent(
            final OrganizationID aOrganizationId,
            final String aTitle,
            final String aDescription,
            final EventType aType,
            final Address aAddress,
            final String aCategoryId,
            final Instant aStartAt,
            final Instant aFinishAt
    ) {
        final var aId = new EventID(ULID.random());
        final var aNow = InstantUtils.now();

        return new Event(
                aId,
                0L,
                aOrganizationId,
                aTitle,
                aDescription,
                EventStatus.SCHEDULED,
                aType,
                aAddress,
                null,
                aCategoryId,
                aStartAt,
                aFinishAt,
                aNow,
                aNow,
                null
        );
    }

    public static Event with(
            final EventID aEventId,
            final long aVersion,
            final OrganizationID aOrganizationId,
            final String aTitle,
            final String aDescription,
            final EventStatus aStatus,
            final EventType aType,
            final Address aAddress,
            final String aImageUrl,
            final String aCategoryId,
            final Instant aStartAt,
            final Instant aFinishAt,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aDeletedAt
    ) {
        return new Event(
                aEventId,
                aVersion,
                aOrganizationId,
                aTitle,
                aDescription,
                aStatus,
                aType,
                aAddress,
                aImageUrl,
                aCategoryId,
                aStartAt,
                aFinishAt,
                aCreatedAt,
                aUpdatedAt,
                aDeletedAt
        );
    }

    public Event markAsDeleted() {
        final var aUpdatedEntity = new Event(
                getId(),
                getVersion(),
                getOrganizationId(),
                getTitle(),
                getDescription().orElse(null),
                getStatus(),
                getType(),
                getAddress().orElse(null),
                getImageUrl().orElse(null),
                getCategoryId(),
                getStartAt(),
                getFinishAt(),
                getCreatedAt(),
                getUpdatedAt(),
                getDeletedAt().orElse(null)
        );

        aUpdatedEntity.setStatus(EventStatus.DELETED);
        aUpdatedEntity.setDeletedAt(InstantUtils.now());
        aUpdatedEntity.setUpdatedAt(InstantUtils.now());

        return aUpdatedEntity;
    }

    public Event update(
            final String aTitle,
            final String aDescription,
            final EventType aType,
            final Address aAddress,
            final String aCategoryId,
            final Instant aStartAt,
            final Instant aFinishAt
    ) {
        final var aUpdatedEntity = new Event(
                getId(),
                getVersion(),
                getOrganizationId(),
                getTitle(),
                getDescription().orElse(null),
                getStatus(),
                getType(),
                getAddress().orElse(null),
                getImageUrl().orElse(null),
                getCategoryId(),
                getStartAt(),
                getFinishAt(),
                getCreatedAt(),
                getUpdatedAt(),
                getDeletedAt().orElse(null)
        );

        aUpdatedEntity.setTitle(aTitle);
        aUpdatedEntity.setDescription(aDescription);
        aUpdatedEntity.setType(aType);
        aUpdatedEntity.setAddress(aAddress);
        aUpdatedEntity.setCategoryId(aCategoryId);
        aUpdatedEntity.setStartAt(aStartAt);
        aUpdatedEntity.setFinishAt(aFinishAt);
        aUpdatedEntity.setUpdatedAt(InstantUtils.now());

        return aUpdatedEntity;
    }

    public OrganizationID getOrganizationId() {
        return organizationId;
    }

    public String getTitle() {
        return title;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public EventStatus getStatus() {
        return status;
    }

    public EventType getType() {
        return type;
    }

    public Optional<Address> getAddress() {
        return Optional.ofNullable(address);
    }

    public Optional<String> getImageUrl() {
        return Optional.ofNullable(imageUrl);
    }

    public String getCategoryId() {
        return categoryId;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getFinishAt() {
        return finishAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    private void setOrganizationId(final OrganizationID organizationId) {
        this.assertArgumentNotNull(organizationId, "organizationId", "should not be null");
        this.organizationId = organizationId;
    }

    private void setTitle(final String title) {
        this.title = title;
    }

    private void setDescription(final String description) {
        this.assertArgumentMaxLength(description, 255, "description", "can only have a maximum of 255 characters");
        this.description = description;
    }

    private void setStatus(final EventStatus status) {
        this.assertArgumentNotNull(status, "status", "should not be null");
        this.status = status;
    }

    private void setType(final EventType type) {
        this.assertArgumentNotNull(type, "type", "should not be null");
        this.type = type;
    }

    private void setAddress(final Address address) {
        if (getType().name().equals(EventType.IN_PERSON.name())) {
            this.address = this.assertArgumentNotNull(address, "address", "should not be null");
            return;
        }
        this.address = address;
    }

    private void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private void setCategoryId(final String categoryId) {
        this.assertArgumentNotEmpty(categoryId, "categoryId", "should not be empty");
        this.categoryId = categoryId;
    }

    private void setStartAt(final Instant startAt) {
        this.assertArgumentNotNull(startAt, "startAt", "should not be null");
//        this.assertArgumentTrue(getStartAt().isBefore(getFinishAt()), "startAt", "must be before finishAt");
        this.startAt = startAt;
    }

    private void setFinishAt(final Instant aFinishAt) {
        // TODO precisamos adicionar uma validação melhor para o start e finish
        this.assertArgumentNotNull(aFinishAt, "finishAt", "should not be null");
        this.assertArgumentTrue(!aFinishAt.isBefore(startAt), "finishAt", "must be after startAt");
        this.finishAt = aFinishAt;
    }

    private void setCreatedAt(final Instant createdAt) {
        this.assertArgumentNotNull(createdAt, "createdAt", "should not be null");
        this.createdAt = createdAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        this.assertArgumentNotNull(updatedAt, "updatedAt", "should not be null");
        this.updatedAt = updatedAt;
    }

    private void setDeletedAt(final Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public void validate(ValidationHandler aHandler) {
    }

    @Override
    public String toString() {
        return "Event(" +
                "id=" + getId().value().toString() +
                ", version='" + getVersion() + '\'' +
                ", organizationId='" + organizationId.value().toString() + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status.name() +
                ", type=" + type.name() +
                ", address=" + getAddress().map(Address::toString).orElse(null) +
                ", imageUrl='" + imageUrl + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", startAt=" + startAt +
                ", finishAt=" + finishAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                ')';
    }

    @Generated
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof Event event)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(organizationId, event.organizationId) && Objects.equals(title, event.title) && Objects.equals(description, event.description) && status == event.status && type == event.type && Objects.equals(address, event.address) && Objects.equals(imageUrl, event.imageUrl) && Objects.equals(categoryId, event.categoryId) && Objects.equals(startAt, event.startAt) && Objects.equals(finishAt, event.finishAt) && Objects.equals(deletedAt, event.deletedAt);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), organizationId, title, description, status, type, address, imageUrl, categoryId, startAt, finishAt, deletedAt);
    }
}
