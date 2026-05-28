import com.oocourse.spec3.exceptions.ColdStartUserException;
import com.oocourse.spec3.exceptions.ColdStartVideoException;
import com.oocourse.spec3.exceptions.DuplicateSubscriptionException;
import com.oocourse.spec3.exceptions.EqualCommentIdException;
import com.oocourse.spec3.exceptions.EqualUserIdException;
import com.oocourse.spec3.exceptions.EqualVideoIdException;
import com.oocourse.spec3.exceptions.FollowLinkNotFoundException;
import com.oocourse.spec3.exceptions.InsufficientCoinsException;
import com.oocourse.spec3.exceptions.InvalidAgeException;
import com.oocourse.spec3.exceptions.InvalidCommentException;
import com.oocourse.spec3.exceptions.InvalidCoinsException;
import com.oocourse.spec3.exceptions.InvalidRankException;
import com.oocourse.spec3.exceptions.InvalidTypeException;
import com.oocourse.spec3.exceptions.NoContributorsException;
import com.oocourse.spec3.exceptions.NoUserException;
import com.oocourse.spec3.exceptions.NoVideoUploadedException;
import com.oocourse.spec3.exceptions.SelfSubscriptionException;
import com.oocourse.spec3.exceptions.UserIdNotFoundException;
import com.oocourse.spec3.exceptions.VideoIdNotFoundException;
import com.oocourse.spec3.exceptions.VideoUnwatchedException;
import com.oocourse.spec3.exceptions.UncessException;
import com.oocourse.spec3.exceptions.DuplicateMedalException;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.UserInterface;
import com.oocourse.spec3.main.VideoInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.ArrayList;

public class Network implements NetworkInterface {
    private final HashMap<Integer, UserInterface> users = new HashMap<>();
    private final HashMap<Integer, VideoInterface> videos = new HashMap<>();
    private int mutualFollowingSum = 0;
    private final HashMap<String, VideoInterface> popularMap = new HashMap<>();
    private final HashMap<Integer, Integer> ldsCache = new HashMap<>();

    public Network() {
    }

    @Override
    public boolean containsUser(int id) {
        return users.containsKey(id);
    }

    @Override
    public UserInterface getUser(int id) {
        return users.get(id);
    }

    @Override
    public boolean containsVideo(int id) {
        return videos.containsKey(id);
    }

    @Override
    public VideoInterface getVideo(int id) {
        return videos.get(id);
    }

    @Override
    public void addUser(int id, String name, int age)
            throws EqualUserIdException, InvalidAgeException {
        if (containsUser(id)) {
            throw new EqualUserIdException(id);
        }
        if (age < 0 || age > 110) {
            throw new InvalidAgeException(age);
        }
        users.put(id, new User(id, name, age));
        System.out.println("add_user succeeded");
    }

    @Override
    public void uploadVideo(int uploaderId, int videoId, String type)
            throws UserIdNotFoundException, EqualVideoIdException, InvalidTypeException {
        if (!containsUser(uploaderId)) {
            throw new UserIdNotFoundException(uploaderId);
        }
        if (containsVideo(videoId)) {
            throw new EqualVideoIdException(videoId);
        }
        if (!isValidType(type)) {
            throw new InvalidTypeException(type);
        }
        Video video = new Video(videoId, uploaderId, type);
        videos.put(videoId, video);
        
        User uploader = (User) users.get(uploaderId);
        uploader.addVideo(videoId);
        for (UserInterface follower : uploader.getFollowers()) {
            ((User) follower).receiveVideo(video);
        }
        updatePopularCache(video, true);
        System.out.println("upload_video succeeded");
    }
    
    @Override
    public boolean isValidType(String type) {
        return type.equals("tech") || type.equals("music") ||
                type.equals("sport") || type.equals("game") ||
                type.equals("food") || type.equals("travel") || type.equals("comedy");
    }
    
    @Override
    public void followUser(int id1, int id2)
            throws UserIdNotFoundException, SelfSubscriptionException,
            DuplicateSubscriptionException {
        if (!containsUser(id1)) {
            throw new UserIdNotFoundException(id1);
        }
        if (!containsUser(id2)) {
            throw new UserIdNotFoundException(id2);
        }
        if (id1 == id2) {
            throw new SelfSubscriptionException(id1);
        }
        User u1 = (User) users.get(id1);
        User u2 = (User) users.get(id2);
        if (u1.isFollowing(u2)) {
            throw new DuplicateSubscriptionException(id1, id2);
        }

        u1.addFollowing(u2);
        u2.addFollower(u1);
        if (u2.isFollowing(u1)) {
            mutualFollowingSum++;
        }
        System.out.println("follow_user succeeded");
    }

    @Override
    public void unfollowUser(int id1, int id2)
            throws UserIdNotFoundException, FollowLinkNotFoundException {
        if (!containsUser(id1)) {
            throw new UserIdNotFoundException(id1);
        }
        if (!containsUser(id2)) {
            throw new UserIdNotFoundException(id2);
        }
        User u1 = (User) users.get(id1);
        User u2 = (User) users.get(id2);
        if (!u1.isFollowing(u2)) {
            throw new FollowLinkNotFoundException(id1, id2);
        }

        if (u2.isFollowing(u1)) {
            mutualFollowingSum--;
        }
        u1.removeFollowing(id2);
        u2.removeFollower(id1);
        System.out.println("unfollow_user succeeded");
    }

    @Override
    public void watchVideo(int userId, int videoId)
            throws UserIdNotFoundException, VideoIdNotFoundException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        User user = (User) users.get(userId);
        Video video = (Video) videos.get(videoId);
        user.watchVideo(video);
        video.addPlayCount(1);
        updatePopularCache(video, true);
        int uploaderId = video.getUploaderId();
        User uploader = (User) getUser(uploaderId);
        uploader.updateInfluence(video.getType(), 2);
        System.out.println("watch_video succeeded");
    }

    @Override
    public List<Integer> queryReceivedUnwatchedVideos(int userId) throws UserIdNotFoundException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        return users.get(userId).queryReceivedUnwatchedVideos();
    }

    @Override
    public double[] queryUpFollowersAgeRatio(int upId) throws UserIdNotFoundException {
        if (!containsUser(upId)) {
            throw new UserIdNotFoundException(upId);
        }
        return users.get(upId).queryAgeRatio();
    }

    @Override
    public int queryMutualFollowingSum() { return mutualFollowingSum; }

    @Override
    public int queryShortestPath(int id1, int id2) throws UserIdNotFoundException, UncessException {
        if (!containsUser(id1)) {
            throw new UserIdNotFoundException(id1);
        }
        if (!containsUser(id2)) {
            throw new UserIdNotFoundException(id2);
        }
        if (id1 == id2) {
            return 0;
        }
        Queue<Integer> queue = new LinkedList<>();
        HashMap<Integer, Integer> dist = new HashMap<>();
        queue.add(id1);
        dist.put(id1, 0);
        
        while (!queue.isEmpty()) {
            int curId = queue.poll();
            int d = dist.get(curId);
            User curUser = (User) users.get(curId);
            for (UserInterface neighbor : curUser.getFollowingList()) {
                int nid = neighbor.getId();
                if (nid == id2) {
                    return d + 1;
                }
                if (!dist.containsKey(nid)) {
                    dist.put(nid, d + 1);
                    queue.add(nid);
                }
            }
        }
        throw new UncessException(id1, id2);
    }
    
    @Override
    public void addUserCoins(int userId, int coins) throws UserIdNotFoundException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        User user = (User) getUser(userId);
        user.addCoins(coins);
        System.out.println("add_user_coins succeeded");
    }
    
    @Override
    public void likeVideo(int userId, int videoId) throws UserIdNotFoundException,
            VideoIdNotFoundException, VideoUnwatchedException, EqualUserIdException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        User user = (User) getUser(userId);
        Video video = (Video) getVideo(videoId);
        int uploaderId = video.getUploaderId();
        User uploader = (User) getUser(uploaderId);
        if (userId == uploaderId) {
            throw new EqualUserIdException(userId);
        }
        if (!user.getWatchedVideos().contains(videoId)) {
            throw new VideoUnwatchedException(userId, videoId);
        }
        if (!user.hasLikedVideo(video)) {
            user.addLikedVideo(video);
            video.addLikes(1);
            updatePopularCache(video, true);
            uploader.updateInfluence(video.getType(), 3);
            System.out.println("like_video succeeded");
        } else {
            user.removeLikedVideo(video);
            video.addLikes(-1);
            updatePopularCache(video, false);
            uploader.updateInfluence(video.getType(), -3);
            System.out.println("unlike_video succeeded");
        }
    }
    
    @Override
    public void coinVideo(int userId, int videoId, int amount)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            InsufficientCoinsException, VideoUnwatchedException,
            InvalidCoinsException, EqualUserIdException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        User user = (User) getUser(userId);
        Video video = (Video) getVideo(videoId);
        int uploaderId = video.getUploaderId();
        if (userId == uploaderId) {
            throw new EqualUserIdException(userId);
        }
        if (!user.getWatchedVideos().contains(videoId)) {
            throw new VideoUnwatchedException(userId, videoId);
        }
        if (amount != 1 && amount != 2) {
            throw new InvalidCoinsException(amount);
        }
        if (user.getCoins() < amount) {
            throw new InsufficientCoinsException(userId);
        }
        
        user.subCoins(amount);
        video.addCoins(amount);
        updatePopularCache(video, true);
        User uploader = (User) getUser(uploaderId);
        uploader.addCoins(amount);
        uploader.addContribution(userId, amount);
        int heatDelta = amount * 5;
        uploader.updateInfluence(video.getType(), heatDelta);
        System.out.println("coin_video succeeded");
    }
    
    @Override
    public int queryBestContributor(int id)
            throws UserIdNotFoundException, NoContributorsException {
        if (!containsUser(id)) {
            throw new UserIdNotFoundException(id);
        }
        User user = (User) getUser(id);
        if (user.getContributors().isEmpty()) {
            throw new NoContributorsException(id);
        }
        return user.getBestContributorId();
    }
    
    @Override
    public void forwardVideo(int userId, int videoId, int followerId)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            FollowLinkNotFoundException, VideoUnwatchedException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsUser(followerId)) {
            throw new UserIdNotFoundException(followerId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        User user = (User) getUser(userId);
        Video video = (Video) getVideo(videoId);
        User follower = (User) getUser(followerId);
        if (!user.hasWatchedVideo(video)) {
            throw new VideoUnwatchedException(userId, videoId);
        }
        if (!user.containsFollower(follower)) {
            throw new FollowLinkNotFoundException(userId, followerId);
        }
        video.addForwardCount(1);
        updatePopularCache(video, true);
        int uploaderId = video.getUploaderId();
        User uploader = (User) getUser(uploaderId);
        uploader.updateInfluence(video.getType(), 4);
        follower.addReceivedVideo(videoId);
        System.out.println("forward_video succeeded");
    }
    
    @Override
    public void sendComment(int userId, int videoId, int commentId, String comment)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            EqualCommentIdException, InvalidCommentException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        Video video = (Video) getVideo(videoId);
        if (video.containsComment(commentId)) {
            throw new EqualCommentIdException(commentId);
        }
        if (comment == null || comment.isEmpty()) {
            throw new InvalidCommentException();
        }
        video.addComment(commentId, comment);
        System.out.println("send_comment succeeded");
    }
    
    @Override
    public int[] cleanSpamComments(int videoId, String keyword) throws VideoIdNotFoundException {
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }

        Video video = (Video) getVideo(videoId);

        return video.cleanSpam(keyword);
    }
    
    @Override
    public VideoInterface queryMostPopularVideo(String type) throws InvalidTypeException {
        if (!isValidType(type)) {
            throw new InvalidTypeException(type);
        }
        return popularMap.get(type);
    }
    
    private void updatePopularCache(VideoInterface video, boolean heatIncreased) {
        // true for up, false for down
        String type = video.getType();
        VideoInterface champion = popularMap.get(type);
        if (heatIncreased || champion == null) {
            if (champion == null) {
                popularMap.put(type, video);
            } else {
                double curHeat = video.getHeat();
                double champHeat = champion.getHeat();
                if (curHeat > champHeat) {
                    popularMap.put(type, video);
                } else if (curHeat == champHeat && video.getId() < champion.getId()) {
                    popularMap.put(type, video);
                }
            }
        }
        else {
            if (video.equals(champion)) {
                reScanCategory(type);
            }
        }
    }
    
    private void reScanCategory(String type) {
        VideoInterface newBest = null;
        double maxHeat = -1.0;
        for (VideoInterface v : videos.values()) {
            if (v.getType().equals(type)) {
                double h = v.getHeat();
                if (h > maxHeat) {
                    maxHeat = h;
                    newBest = v;
                } else if (h == maxHeat) {
                    if (newBest == null || v.getId() < newBest.getId()) {
                        newBest = v;
                    }
                }
            }
        }
        popularMap.put(type, newBest);
    }
    
    @Override
    public void purchaseMedal(int userId, int videoId, int amount)
            throws UserIdNotFoundException, EqualUserIdException,
            VideoIdNotFoundException, InsufficientCoinsException,
            DuplicateMedalException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        User user = (User) getUser(userId);
        Video video = (Video) getVideo(videoId);
        int uploaderId = video.getUploaderId();
        if (userId == uploaderId) {
            throw new EqualUserIdException(userId);
        }
        if (user.getCoins() < amount) {
            throw new InsufficientCoinsException(userId);
        }
        if (user.hasMedal(uploaderId)) {
            throw new DuplicateMedalException(userId, uploaderId);
        }
        
        user.subCoins(amount);
        user.addMedal(uploaderId);
        User uploader = (User) getUser(uploaderId);
        uploader.addCoins(amount);
        System.out.println("purchase_medal succeeded");
    }
    
    @Override
    public int queryLongestDecSeq() {
        if (users.isEmpty()) {
            return 0;
        }
        ldsCache.clear();
        int maxLen = 0;
        for (UserInterface user : users.values()) {
            maxLen = Math.max(maxLen, getLds((User) user));
        }
        return maxLen;
    }
    
    @Override
    public int[] queryGlobalBestContributor() throws NoUserException {
        if (users.isEmpty()) {
            throw new NoUserException();
        }
        HashMap<Integer, Integer> freqMap = new HashMap<>();
        boolean hasAnyContributor = false;
        
        for (UserInterface user : users.values()) {
            User u = (User) user;
            if (!u.getContributors().isEmpty()) {
                hasAnyContributor = true;
                int bestId = u.getBestContributorId();
                freqMap.put(bestId, freqMap.getOrDefault(bestId, 0) + 1);
            }
        }
        if (!hasAnyContributor) {
            return new int[] {0, 0};
        }
        
        int maxFreq = 0;
        int minId = Integer.MAX_VALUE;
        
        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            int id = entry.getKey();
            int freq = entry.getValue();
            
            if (freq > maxFreq || (freq == maxFreq && id < minId)) {
                maxFreq = freq;
                minId = id;
            }
        }
        
        return new int[] {minId, maxFreq};
    }
    
    @Override
    public int recommendVideo(int userId) throws UserIdNotFoundException,
            NoVideoUploadedException, ColdStartVideoException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (videos.isEmpty()) {
            throw new NoVideoUploadedException();
        }
        User user = (User) users.get(userId);
        if (user.getWatchedVideos().isEmpty()) {
            throw new ColdStartVideoException(userId);
        }
        
        int totalV = videos.size();
        
        HashMap<String, Integer> interestMap = new HashMap<>();
        String[] types = {"tech", "music", "sport", "game", "food", "travel", "comedy"};
        for (String t : types) {
            interestMap.put(t, user.getInterest(t, totalV));
        }
        
        int bestId = -1;
        long maxScore = -1;
        
        for (VideoInterface v : videos.values()) {
            int heat = v.getHeat();
            double interest = interestMap.get(v.getType());
            long score = (long) (heat * interest);
            
            if (score > maxScore || (score == maxScore &&
                    (bestId == -1 || v.getId() < bestId))) {
                maxScore = score;
                bestId = v.getId();
            }
        }
        return bestId;
    }
    
    @Override
    public long computeVideoScore(UserInterface user, VideoInterface video) {
        int heat = video.getHeat();
        double interest = (user).getInterest(video.getType(), videos.size());
        return (long) (heat * interest);
    }
    
    @Override
    public int recommendNthUp(int userId, int rank) throws UserIdNotFoundException,
            InvalidRankException, NoVideoUploadedException, ColdStartUserException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (rank <= 0) {
            throw new InvalidRankException(rank);
        }
        if (videos.isEmpty()) {
            throw new NoVideoUploadedException();
        }
        
        User user = (User) getUser(userId);
        List<UserInterface> candidates = new ArrayList<>();
        for (UserInterface u : users.values()) {
            if (u.getId() != userId && !user.isFollowing(u)) {
                candidates.add(u);
            }
        }
        if (candidates.size() < rank) {
            throw new ColdStartUserException(userId);
        }

        int totalV = videos.size();
        candidates.sort((u1, u2) -> {
            long score1 = user.computeUpScore(u1, totalV);
            long score2 = user.computeUpScore(u2, totalV);
            if (score1 != score2) {
                return Long.compare(score2, score1);
            }
            return Integer.compare(u1.getId(), u2.getId());
        });

        return candidates.get(rank - 1).getId();
    }
    
    @Override
    public int queryMostInfluentialUp(String type) throws InvalidTypeException,
            NoUserException {
        if (!isValidType(type)) {
            throw new InvalidTypeException(type);
        }
        if (users.isEmpty()) {
            throw new NoUserException();
        }
        
        int bestId = -1;
        int maxInfluence = -1;
        
        for (UserInterface u : users.values()) {
            int curInf = u.getInfluence(type);
            if (curInf > maxInfluence) {
                maxInfluence = curInf;
                bestId = u.getId();
            } else if (curInf == maxInfluence) {
                if (bestId == -1 || u.getId() < bestId) {
                    bestId = u.getId();
                }
            }
        }
        return bestId;
    }
    
    @Override
    public List<Integer> queryUserProfile(int userId) throws UserIdNotFoundException,
            ColdStartVideoException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        User user = (User) getUser(userId);
        if (user.getWatchedVideos().isEmpty()) {
            throw new ColdStartVideoException(userId);
        }
        
        return user.getProfile(videos.size());
    }
    
    private int getLds(User u) {
        if (ldsCache.containsKey(u.getId())) {
            return ldsCache.get(u.getId());
        }
        int currentMax = 0;
        for (UserInterface neighbor : u.getFollowingList()) {
            if (u.getAge() > neighbor.getAge()) {
                currentMax = Math.max(currentMax, getLds((User) neighbor));
            }
        }
        int result = 1 + currentMax;
        ldsCache.put(u.getId(), result);
        return result;
    }
    
    public UserInterface[] getUsers() {
        return users.values().toArray(new UserInterface[0]);
    }
}
