package us.ajg0702.queue.api.communication;

import com.google.common.io.ByteArrayDataInput;

import java.util.UUID;

public class ComResponse {
    private String from;
    private String response;
    private String identifier;
    private String noneMessage;

    private ComResponse(String from, String response) {
        this.from = from;
        this.response = response;
    }

    public static ComResponse from(String from) {
        return new ComResponse(from, null);
    }
    public static ComResponse from(String from, ByteArrayDataInput in) {
        String id = in.readUTF();
        String response = in.readUTF();
        String noneMessage = in.readUTF();

        if(id.equalsIgnoreCase("null")) id = null;
        if(response.equalsIgnoreCase("null")) response = null;
        if(noneMessage.equalsIgnoreCase("null")) noneMessage = null;

        return from(from)
                .id(id)
                .with(response)
                .noneMessage(noneMessage);
    }
    public ComResponse id(String id) {
        this.identifier = id;
        return this;
    }
    public ComResponse id(UUID id) {
        this.identifier = String.valueOf(id);
        return this;
    }
    public ComResponse with(String response) {
        this.response = response;
        return this;
    }
    public ComResponse with(boolean response) {
        this.response = String.valueOf(response);
        return this;
    }
    public ComResponse with(Integer response) {
        this.response = String.valueOf(response);
        return this;
    }

    public ComResponse noneMessage(String message) {
        this.noneMessage = message;
        return this;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getFrom() {
        return from;
    }

    public String getNoneMessage() {
        return noneMessage;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "ComResponse{" +
                "from='" + from + '\'' +
                ", response='" + response + '\'' +
                ", identifier='" + identifier + '\'' +
                ", noneMessage='" + noneMessage + '\'' +
                '}';
    }
}
