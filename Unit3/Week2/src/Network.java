import com.oocourse.spec2.exceptions.UserIdNotFoundException;
import com.oocourse.spec2.exceptions.VideoUnwatchedException;
import com.oocourse.spec2.exceptions.NoContributorsException;
import com.oocourse.spec2.exceptions.FollowLinkNotFoundException;
import com.oocourse.spec2.exceptions.InsufficientCoinsException;
import com.oocourse.spec2.exceptions.InvalidCommentException;
import com.oocourse.spec2.exceptions.VideoIdNotFoundException;
import com.oocourse.spec2.exceptions.InvalidTypeException;
import com.oocourse.spec2.exceptions.DuplicateMedalException;
import com.oocourse.spec2.exceptions.DuplicateSubscriptionException;
import com.oocourse.spec2.exceptions.UncessException;
import com.oocourse.spec2.exceptions.EqualUserIdException;
import com.oocourse.spec2.exceptions.InvalidAgeException;
import com.oocourse.spec2.exceptions.EqualVideoIdException;
import com.oocourse.spec2.exceptions.SelfSubscriptionException;
import com.oocourse.spec2.exceptions.InvalidCoinsException;
import com.oocourse.spec2.exceptions.EqualCommentIdException;
import com.oocourse.spec2.main.NetworkInterface;
import com.oocourse.spec2.main.UserInterface;
import com.oocourse.spec2.main.VideoInterface;

import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;

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
        for (UserInterface follower : uploader.getFollowers()) {
            ((User) follower).receiveVideo(videoId);
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
        user.watchVideo(videoId);
        video.addPlayCount(1);
        updatePopularCache(video, true);
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
            System.out.println("like_video succeeded");
        } else {
            user.removeLikedVideo(video);
            video.addLikes(-1);
            updatePopularCache(video, false);
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
