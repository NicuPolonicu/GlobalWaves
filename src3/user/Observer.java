package user;

public interface Observer {

    /**
     * Add a new notification to the user's list.
     */
    void updateNotifications(Notification notification);
}
