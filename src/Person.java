public abstract class Person {
    protected final String id;
    protected final String name;

    public Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Template method pattern - abstract method to be implemented by subclasses
    public abstract String getDisplayInfo();

    // Common functionality for all entities
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
                name != null && !name.trim().isEmpty();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return String.format("%s{id='%s', name='%s'}",
                getClass().getSimpleName(), id, name);
    }
}