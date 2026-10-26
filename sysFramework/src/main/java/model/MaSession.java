package main.java.model;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

/**
 * Wrapper de session utilisable dans les controllers
 * sans dépendre directement de HttpSession dans le code métier
 * (sauf via from / flushTo gérés par le framework).
 */
public class MaSession {

    private String id;
    private Map<String, Object> attributes = new HashMap<>();
    private Set<String> initialKeys = new HashSet<>();

    public MaSession() {
    }

    /**
     * Crée une MaSession à partir de la vraie HttpSession.
     */
    public static MaSession from(HttpSession httpSession) {
        MaSession ma = new MaSession();
        if (httpSession == null) {
            return ma;
        }

        ma.id = httpSession.getId();

        Enumeration<String> names = httpSession.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object value = httpSession.getAttribute(name);
            ma.attributes.put(name, value);
            ma.initialKeys.add(name);
        }

        // Variante avec asIterator() si tu préfères :
        // httpSession.getAttributeNames().asIterator()
        //     .forEachRemaining(name -> {
        //         ma.attributes.put(name, httpSession.getAttribute(name));
        //         ma.initialKeys.add(name);
        //     });

        return ma;
    }

    /**
     * Répercute les modifications de MaSession dans la vraie HttpSession.
     */
    public void flushTo(HttpSession httpSession) {
        if (httpSession == null) {
            return;
        }

        // 1. Supprimer les clés qui existaient au départ mais plus dans attributes
        for (String key : initialKeys) {
            if (!attributes.containsKey(key)) {
                httpSession.removeAttribute(key);
            }
        }

        // 2. Mettre à jour / ajouter toutes les valeurs actuelles
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            httpSession.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    // ===== API développeur =====

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}