import com.oocourse.spec3.main.UserInterface;
import com.oocourse.spec3.main.VideoInterface;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;

public class User implements UserInterface {
    private final int id;
    private final String name;
    private final int age;
    private int coins = 0;

    private final HashMap<Integer, UserInterface> following = new HashMap<>();
    private final HashMap<Integer, UserInterface> followers = new HashMap<>();
    private final HashSet<Integer> contributors = new HashSet<>();
    private final HashMap<Integer, Integer> contributions = new HashMap<>();
    private final LinkedList<Integer> receivedVideosList = new LinkedList<>();
    private final HashSet<Integer> unreadVideos = new HashSet<>();
    private final HashSet<Integer> watchedVideos = new HashSet<>();
    private final HashSet<Integer> likedVideos = new HashSet<>();
    private final HashSet<Integer> medals = new HashSet<>();
    private final HashMap<String, Integer> typeCounts = new HashMap<>();
    private final HashSet<Integer> videos = new HashSet<>();
    private final HashMap<String, Integer> influenceMap = new HashMap<>();
    
    private int bestContributorId = -1;
    private int maxContribution = -1;
    private final int[] ageSegments = new int[4];
    
    private int getAgeSegmentIndex(int age) {
        if (age <= 16) {
            return 0;
        }
        if (age <= 30) {
            return 1;
        }
        if (age <= 45) {
            return 2;
        }
        return 3;
    }

    public User(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    public Collection<UserInterface> getFollowingList() {
        return following.values();
    }

    @Override
    public boolean isFollowing(UserInterface user) {
        return following.containsKey(user.getId());
    }

    @Override
    public boolean containsFollower(UserInterface user) {
        return followers.containsKey(user.getId());
    }
    
    @Override
    public boolean hasReceivedVideo(VideoInterface video) {
        return unreadVideos.contains(video.getId());
    }

    public void addFollowing(UserInterface user) {
        following.put(user.getId(), user);
    }

    public void addFollower(UserInterface user) {
        followers.put(user.getId(), user);
        ageSegments[getAgeSegmentIndex(user.getAge())]++;
    }

    public void removeFollowing(int id) {
        following.remove(id);
    }

    public void removeFollower(int id) {
        UserInterface removed = followers.remove(id);
        if (removed != null) {
            ageSegments[getAgeSegmentIndex(removed.getAge())]--;
        }
    }

    public void receiveVideo(VideoInterface video) {
        int videoId = video.getId();
        receivedVideosList.addFirst(videoId);
        unreadVideos.add(videoId);
    }
    
    public void watchVideo(VideoInterface video) {
        int videoId = video.getId();
        unreadVideos.remove(videoId);
        receivedVideosList.removeIf(id -> id == videoId);
        watchedVideos.add(videoId);
        String type = video.getType();
        typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
    }
    
    @Override
    public double[] queryAgeRatio() {
        double[] ratios = new double[4];
        int totalFollowers = followers.size();
        if (totalFollowers == 0) {
            return ratios;
        }
        for (int i = 0; i < 4; i++) {
            ratios[i] = (double) ageSegments[i] / totalFollowers;
        }
        return ratios;
    }
    
    @Override
    public boolean hasWatchedVideo(VideoInterface video) {
        return watchedVideos.contains(video.getId());
    }
    
    public HashSet<Integer> getWatchedVideos() {
        return watchedVideos;
    }
    
    @Override
    public boolean hasLikedVideo(VideoInterface video) {
        return likedVideos.contains(video.getId());
    }
    
    public void addLikedVideo(VideoInterface video) {
        likedVideos.add(video.getId());
    }
    
    public void removeLikedVideo(VideoInterface video) {
        likedVideos.remove(video.getId());
    }
    
    public void addReceivedVideo(int videoId) {
        receivedVideosList.addFirst(videoId);
        unreadVideos.add(videoId);
    }
    
    @Override
    public int getCoins() {
        return this.coins;
    }
    
    public void addCoins(int num) {
        this.coins += num;
    }
    
    public void subCoins(int num) {
        this.coins -= num;
    }
    
    @Override
    public boolean hasMedal(int uploaderId) {
        return medals.contains(uploaderId);
    }
    
    @Override
    public int getInterest(String type, int totalVideos) {
        int typeCount = typeCounts.getOrDefault(type, 0);
        int watchedCount = watchedVideos.size();
        return typeCount * (totalVideos - watchedCount + 1);
    }
    
    @Override
    public int getInfluence(String type) {
        return influenceMap.getOrDefault(type, 0);
    }
    
    public void updateInfluence(String type, int heatDelta) {
        int oldHeat = influenceMap.getOrDefault(type, 0);
        influenceMap.put(type, oldHeat + heatDelta);
    }
    
    @Override
    public List<Integer> getProfile(int totalVideos) {
        String[] types = {"tech", "music", "sport", "game", "food", "travel", "comedy"};
        List<Integer> profile = new ArrayList<>();
        for (String type : types) {
            profile.add(getInterest(type, totalVideos));
        }
        return profile;
    }
    
    @Override
    public long computeUpScore(UserInterface up, int totalVideos) {
        String[] types = {"tech", "music", "sport", "game", "food", "travel", "comedy"};
        long totalScore = 0;
        for (String type : types) {
            long interest = this.getInterest(type, totalVideos);
            long influence = up.getInfluence(type);
            totalScore += (interest * influence);
        }
        
        return totalScore;
    }
    
    public void addMedal(int uploaderId) {
        medals.add(uploaderId);
    }
    
    @Override
    public List<Integer> queryReceivedUnwatchedVideos() {
        List<Integer> res = new LinkedList<>();
        for (Integer vid : receivedVideosList) {
            if (unreadVideos.contains(vid)) {
                res.add(vid);
                if (res.size() == 5) {
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserInterface) {
            return ((UserInterface) obj).getId() == id;
        }
        return false;
    }

    public Collection<UserInterface> getFollowers() {
        return followers.values();
    }
    
    public HashSet<Integer> getContributors() {
        return contributors;
    }
    
    public int getBestContributorId() {
        return bestContributorId;
    }
    
    public void addContribution(int userId, int amount) {
        int newTotal = contributions.getOrDefault(userId, 0) + amount;
        contributors.add(userId);
        contributions.put(userId, newTotal);
        if (newTotal > maxContribution) {
            maxContribution = newTotal;
            bestContributorId = userId;
        } else if (newTotal == maxContribution) {
            if (userId < bestContributorId) {
                bestContributorId = userId;
            }
        }
    }
    
    public void addVideo(int videoId) {
        videos.add(videoId);
    }
    
    public HashSet<Integer> getVideos() {
        return videos;
    }
    
    public boolean strictEquals(UserInterface user) {
        if (user instanceof User) {
            User u = (User) user;
            return id == u.id && name.equals(u.name) && age == u.age &&
                    following.equals(u.following) && followers.equals(u.followers) &&
                    unreadVideos.equals(u.unreadVideos) &&
                    receivedVideosList.equals(u.receivedVideosList);
        }
        return false;
    }
}