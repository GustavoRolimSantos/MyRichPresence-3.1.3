package net.strukteon.myrpc.websocket;

import com.google.gson.*;

public class SocketMessage
{
    public Service service;
    public State state;
    public String title;
    public String author;
    public int remaining;
    
    public SocketMessage() {
        this.service = Service.END;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof SocketMessage && ((SocketMessage)obj).service == this.service && ((SocketMessage)obj).state == this.state && ((SocketMessage)obj).title.equals(this.title) && ((SocketMessage)obj).author.equals(this.author) && ((SocketMessage)obj).remaining == this.remaining;
    }
    
    public String toJSON() {
        return new GsonBuilder().create().toJson(this);
    }
    
    public SocketMessage fromJSON(final String json) {
        return new GsonBuilder().create().fromJson(json, SocketMessage.class);
    }
    
    public enum Service
    {
        END("end", (String)null, (String)null), 
        YOUTUBE("youtube", "557619972738777102", "logo");
        
        String key;
        String imageKey;
        String appId;
        
        private Service(final String key, final String appId, final String imageKey) {
            this.key = key;
            this.appId = appId;
            this.imageKey = imageKey;
        }
        
        public String getKey() {
            return this.key;
        }
        
        public String getAppId() {
            return this.appId;
        }
        
        public String getImageKey() {
            return this.imageKey;
        }
    }
    
    public enum State
    {
        PLAYING("play"), 
        PAUSED("pause"), 
        NONE((String)null);
        
        String key;
        
        private State(final String key) {
            this.key = key;
        }
        
        public String getKey() {
            return this.key;
        }
    }
}
