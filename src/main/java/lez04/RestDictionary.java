package lez04;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@Path("/dictionary")
public class RestDictionary {

    @POST
    @Path("/add")
    @Consumes({"application/xml", "application/json"})
    public Response addEntry(WordMessage message) {
        try {
            Dictionary.getInstance().addElement(message.getWord(), message.getDescription());
        } catch (KeyAlreadyExistsException e) {
            return Response.status(408).build();
        }
        return Response.ok(message).build();
    }

    @GET
    @Path("/search/{word}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getEntry(@PathParam("word") String word) {
        String description;
        try {
            description = Dictionary.getInstance().getElement(word);
        } catch (KeyNotFoundException e) {
            return Response.status(404).build();
        }
        return Response.ok(description).build();
    }

    @PUT
    @Path("/update")
    @Consumes({"application/xml", "application/json"})
    public Response updateEntry(WordMessage message) {
        try {
            Dictionary.getInstance().updateElement(message.getWord(), message.getDescription());
        } catch (KeyNotFoundException e) {
            return Response.status(404).build();
        }
        return Response.ok(message).build();
    }

    @DELETE
    @Path("/delete")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response deleteEntry(String word) {
        try {
            Dictionary.getInstance().removeElement(word);
        } catch (KeyNotFoundException e) {
            return Response.status(404).build();
        }
        return Response.ok().build();
    }
}

class Dictionary {
    private final HashMap<String, String> dictionary = new HashMap<>();
    private static final Dictionary instance = new Dictionary();

    public static Dictionary getInstance() {
        return instance;
    }

    public synchronized void addElement(String word, String description) throws KeyAlreadyExistsException {
        if (this.checkElementExists(word)) {
            throw new KeyAlreadyExistsException();
        }
        this.dictionary.put(word, description);
    }

    public synchronized void updateElement(String word, String description) throws KeyNotFoundException {
        if (!this.checkElementExists(word)) {
            throw new KeyNotFoundException();
        }
        this.dictionary.replace(word, description);
    }

    public synchronized void removeElement(String word) throws KeyNotFoundException {
        if (!this.checkElementExists(word)) {
            throw new KeyNotFoundException();
        }
        this.dictionary.remove(word);
    }

    public synchronized String getElement(String word) throws KeyNotFoundException {
        if (!this.checkElementExists(word)) {
            throw new KeyNotFoundException();
        }
        return this.dictionary.get(word);
    }

    private boolean checkElementExists(String word) {
        for (Map.Entry<String, String> entry :
                this.dictionary.entrySet()) {
            if (entry.getKey().equals(word)) {
                return true;
            }
        }
        return false;
    }
}

@XmlRootElement
class WordMessage {
    String word, description;

    public WordMessage() {
    }

    public WordMessage(String word, String description) {
        this.word = word;
        this.description = description;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

class KeyNotFoundException extends Exception {
}

class KeyAlreadyExistsException extends Exception {
}
