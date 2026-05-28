import com.oocourse.spec3.main.VideoInterface;

import java.util.ArrayList;

public class Video implements VideoInterface {
    private final int id;
    private final int uploaderId;
    private final String type;
    
    private int playCount = 0;
    private int likes = 0;
    private int forwardCount = 0;
    private int coins = 0;
    private final ArrayList<Integer> commentIds = new ArrayList<>();
    private final ArrayList<String> commentContents = new ArrayList<>();

    public Video(int id, int uploaderId, String type) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.type = type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getUploaderId() {
        return uploaderId;
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public int getPlayCount() {
        return playCount;
    }
    
    @Override
    public int getLikes() {
        return likes;
    }
    
    @Override
    public int getForwardCount() {
        return forwardCount;
    }
    
    @Override
    public int getCoins() {
        return coins;
    }
    
    @Override
    public int getHeat() {
        return playCount * 2 + likes * 3 + forwardCount * 4 + coins * 5;
    }
    
    @Override
    public boolean containsComment(int id) {
        return commentIds.contains(id);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VideoInterface) {
            return ((VideoInterface) obj).getId() == id;
        }
        return false;
    }
    
    public void addPlayCount(int amount) {
        this.playCount += amount;
    }
    
    public void addLikes(int amount) {
        this.likes += amount;
    }
    
    public void addCoins(int amount) {
        this.coins += amount;
    }
    
    public void addForwardCount(int amount) {
        this.forwardCount += amount;
    }
    
    public void addComment(int commentId, String comment) {
        commentIds.add(commentId);
        commentContents.add(comment);
    }
    
    public int[] cleanSpam(String keyword) {
        int deleteCount = 0;
        int maxOccur = 0;
        int keyLen = keyword.length();
        ArrayList<Integer> nextIds = new ArrayList<>();
        ArrayList<String> nextContents = new ArrayList<>();
        for (int i = 0; i < commentIds.size(); i++) {
            String content = commentContents.get(i);
            if (content.contains(keyword)) {
                int count = 0;
                for (int j = 0; j <= content.length() - keyLen; j++) {
                    if (content.substring(j, j + keyLen).equals(keyword)) {
                        count++;
                    }
                }
                if (count > maxOccur) {
                    maxOccur = count;
                }
                deleteCount++;
            } else {
                nextIds.add(commentIds.get(i));
                nextContents.add(content);
            }
        }
        this.commentIds.clear();
        this.commentIds.addAll(nextIds);
        this.commentContents.clear();
        this.commentContents.addAll(nextContents);
        
        return new int[]{deleteCount, maxOccur};
    }
    
    public int[] getCommentIds() {
        int i = 0;
        int[] res = new int[commentIds.size()];
        for (int commentId : commentIds) {
            res[i++] = commentId;
        }
        return res;
    }
    
    public String[] getCommentContents() {
        int i = 0;
        String[] res = new String[commentContents.size()];
        for (String commentContent : commentContents) {
            res[i++] = commentContent;
        }
        return res;
    }
}
