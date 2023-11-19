package beep.app.search;

public class ContactItem {
    private String contactName;
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

    public ContactItem() {
    }

    public ContactItem(String contactName, boolean hasUser) {
        this.contactName = contactName;
        this.hasUser = hasUser;
    }
}
