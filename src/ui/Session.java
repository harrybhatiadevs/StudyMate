package ui;

import model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Simple in-memory session holder with listeners. */
public class Session {
    private static final Session INSTANCE = new Session();
    public static Session get() { return INSTANCE; }

    private User current;
    private final List<Consumer<User>> listeners = new ArrayList<>();

    public User currentUser() { return current; }

    public void setUser(User u) {
        this.current = u;
        for (var l : listeners) l.accept(u);
    }

    public void onChange(Consumer<User> l) { listeners.add(l); }
    public void logout() { setUser(null); }
}
