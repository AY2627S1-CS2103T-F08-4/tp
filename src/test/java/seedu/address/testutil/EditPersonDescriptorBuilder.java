package seedu.address.testutil;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Address;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.CreditScore;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * A utility class to help with building EditPersonDescriptor objects.
 */
public class EditPersonDescriptorBuilder {

    private EditPersonDescriptor descriptor;

    public EditPersonDescriptorBuilder() {
        descriptor = new EditPersonDescriptor();
    }

    public EditPersonDescriptorBuilder(EditPersonDescriptor descriptor) {
        this.descriptor = new EditPersonDescriptor(descriptor);
    }

    /**
     * Returns an {@code EditPersonDescriptor} with fields containing {@code person}'s details
     */
    public EditPersonDescriptorBuilder(Person person) {
        descriptor = new EditPersonDescriptor();
        descriptor.setName(person.getName());
        descriptor.setPhone(person.getPhone());
        descriptor.setEmail(person.getEmail());
        descriptor.setAddress(person.getAddress());
        descriptor.setTags(person.getTags());
        descriptor.setClientReference(person.getClientReference());
        descriptor.setCreditScore(person.getCreditScore());
        descriptor.setDebt(person.getDebt());
        if (person.getCardDetails() != null) {
            descriptor.setCardNumber(person.getCardDetails().getCardNumber());
            descriptor.setCvv(person.getCardDetails().getCvv());
            descriptor.setExpiryDate(person.getCardDetails().getExpiryDate());
            descriptor.setProvider(person.getCardDetails().getProvider());
        }
    }

    /**
     * Sets the {@code Name} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withName(String name) {
        descriptor.setName(new Name(name));
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withPhone(String phone) {
        descriptor.setPhone(new Phone(phone));
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withEmail(String email) {
        descriptor.setEmail(new Email(email));
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code EditPersonDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withAddress(String address) {
        descriptor.setAddress(new Address(address));
        return this;
    }

    public EditPersonDescriptorBuilder withClientReference(String reference) {
        descriptor.setClientReference(new ClientReference(reference));
        return this;
    }

    public EditPersonDescriptorBuilder withCardNumber(String cardNumber) {
        descriptor.setCardNumber(cardNumber);
        return this;
    }

    public EditPersonDescriptorBuilder withCvv(String cvv) {
        descriptor.setCvv(cvv);
        return this;
    }

    public EditPersonDescriptorBuilder withExpiryDate(String expiryDate) {
        descriptor.setExpiryDate(expiryDate);
        return this;
    }

    public EditPersonDescriptorBuilder withProvider(String provider) {
        descriptor.setProvider(provider);
        return this;
    }

    public EditPersonDescriptorBuilder withCreditScore(String creditScore) {
        descriptor.setCreditScore(new CreditScore(creditScore));
        return this;
    }

    public EditPersonDescriptorBuilder withDebt(String debt) {
        descriptor.setDebt(new Debt(debt));
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code EditPersonDescriptor}
     * that we are building.
     */
    public EditPersonDescriptorBuilder withTags(String... tags) {
        Set<Tag> tagSet = Stream.of(tags).map(Tag::new).collect(Collectors.toSet());
        descriptor.setTags(tagSet);
        return this;
    }

    public EditPersonDescriptor build() {
        return descriptor;
    }
}
