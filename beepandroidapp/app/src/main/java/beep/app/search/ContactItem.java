package beep.app.search;

public class ContactItem {
    private String contactName;
    private String phoneNumber;
    private boolean hasUser;

    public String getContactName() {
        return contactName;
    }

    public boolean isHasUser() {
        return hasUser;
    }

    public void setHasUser(boolean hasUser) {
        this.hasUser = hasUser;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public ContactItem() {
    }

    public ContactItem(String contactName,String phoneNumber, boolean hasUser) {
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.hasUser = hasUser;
    }
}
