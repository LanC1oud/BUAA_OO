import com.oocourse.spec1.exceptions.EqualUserIdException;
import com.oocourse.spec1.exceptions.DuplicateSubscriptionException;
import com.oocourse.spec1.exceptions.UncessException;
import com.oocourse.spec1.exceptions.EqualVideoIdException;
import com.oocourse.spec1.exceptions.FollowLinkNotFoundException;
import com.oocourse.spec1.exceptions.InvalidAgeException;
import com.oocourse.spec1.exceptions.SelfSubscriptionException;
import com.oocourse.spec1.exceptions.UserIdNotFoundException;
import com.oocourse.spec1.exceptions.VideoIdNotFoundException;
import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.UserInterface;
import com.oocourse.spec1.main.VideoInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

public class Network implements NetworkInterface {
    private final HashMap<Integer, UserInterface> users = new HashMap<>();
    private final HashMap<Integer, VideoInterface> videos = new HashMap<>();
    private int mutualFollowingSum = 0;

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
    public void uploadVideo(int uploaderId, int videoId)
            throws UserIdNotFoundException, EqualVideoIdException {
        if (!containsUser(uploaderId)) {
            throw new UserIdNotFoundException(uploaderId);
        }
        if (containsVideo(videoId)) {
            throw new EqualVideoIdException(videoId);
        }
        Video video = new Video(videoId, uploaderId);
        videos.put(videoId, video);

        User uploader = (User) users.get(uploaderId);
        for (UserInterface follower : uploader.getFollowers()) {
            ((User) follower).receiveVideo(videoId);
        }
        System.out.println("upload_video succeeded");
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
        ((User) users.get(userId)).watchVideo(videoId);
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

    public UserInterface[] getUsers() {
        return users.values().toArray(new UserInterface[0]);
    }
}
