package com.sorcix.sirc;

import net.acomputerdog.core.hash.Hasher;

public abstract class Chattable {
    private String nameLower = null;

    public abstract void send(String message);

    public abstract void sendAction(String action);

    public abstract String getName();

    public String getNameLower() {
        if (nameLower == null) {
            nameLower = getName().toLowerCase();
        }
        return nameLower;
    }

    @Override
    public int hashCode() {
        return new Hasher().hash(getNameLower()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Chattable)) return false;
        return getNameLower().equals(((Chattable) obj).getNameLower());
    }
}
