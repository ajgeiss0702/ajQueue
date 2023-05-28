package us.ajg0702.queue.api.spigot;

public class MessagedResponse<T> {
    private final T response;
    private final String none;

    public MessagedResponse(T response, String none) {
        this.response = response;
        this.none = none;
    }

    /**
     * Gets the response from the method.
     * @return The response. Null if there is a "none" message
     */
    public T getResponse() {
        return response;
    }

    /**
     * Gets the "none" message
     * @return The none message. null if there is a response.
     */
    public String getNone() {
        return none;
    }

    /**
     * Gets either the response (as a string) or the none message, whichever is available
     * @return A string
     */
    public String getEither() {
        if(response == null) return none;
        return String.valueOf(response);
    }
}
