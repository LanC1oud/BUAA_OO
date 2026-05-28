import com.oocourse.spec3.main.UserInterface;
import com.oocourse.spec3.main.VideoInterface;
import com.oocourse.spec3.exceptions.*;
import org.junit.Assert;
import org.junit.Test;
import java.util.*;

public class RecommendNthUpTest {
    
    @Test
    public void testRecommendNthUpRigorous() throws Exception {
        Network nw = new Network();
        nw.addUser(1, "Q", 20);
        nw.addUser(2, "A", 21);
        nw.addUser(3, "B", 22);
        nw.addUser(4, "C", 23);
        nw.addUser(5, "F", 24);
        
        nw.uploadVideo(2, 101, "tech");
        nw.uploadVideo(3, 102, "tech");
        nw.uploadVideo(4, 103, "music");
        
        nw.watchVideo(1, 101);
        nw.watchVideo(1, 102);
        nw.followUser(1, 5);
        
        int oldNetSize = nw.getUsers().length;
        int oldMutual = nw.queryMutualFollowingSum();
        Map<Integer, Integer> coinsSnap = new HashMap<>();
        Map<Integer, Integer> ageSnap = new HashMap<>();
        for (UserInterface u : nw.getUsers()) {
            coinsSnap.put(u.getId(), u.getCoins());
            ageSnap.put(u.getId(), u.getAge());
        }
        
        VideoInterface v101 = nw.getVideo(101);
        int v101Play = v101.getPlayCount();
        int v101Likes = v101.getLikes();
        
        int res1 = nw.recommendNthUp(1, 1);
        int res2 = nw.recommendNthUp(1, 2);
        int res3 = nw.recommendNthUp(1, 3);
        
        Assert.assertTrue(res1 == 2 || res1 == 3);
        Assert.assertTrue(res2 == 2 || res2 == 3);
        Assert.assertNotEquals(res1, res2);
        Assert.assertEquals(4, res3);
        
        UserInterface[] usersAfter = nw.getUsers();
        Assert.assertEquals(oldNetSize, usersAfter.length);
        Assert.assertEquals(oldMutual, nw.queryMutualFollowingSum());
        
        for (UserInterface u : usersAfter) {
            int id = u.getId();
            Assert.assertEquals((int) coinsSnap.get(id), u.getCoins());
            Assert.assertEquals((int) ageSnap.get(id), u.getAge());
        }
        
        VideoInterface vNow = nw.getVideo(101);
        Assert.assertEquals(v101Play, vNow.getPlayCount());
        Assert.assertEquals(v101Likes, vNow.getLikes());
    }
    
    @Test
    public void testRecommendNthUpTieBreaker() throws Exception {
        Network nw = new Network();
        nw.addUser(1, "Q", 20);
        nw.addUser(10, "A", 21);
        nw.addUser(5, "B", 21);
        nw.uploadVideo(10, 101, "tech");
        nw.uploadVideo(5, 102, "tech");
        nw.watchVideo(1, 101);
        nw.watchVideo(1, 102);
        
        Assert.assertEquals(5, nw.recommendNthUp(1, 1));
        Assert.assertEquals(10, nw.recommendNthUp(1, 2));
    }
    
    @Test(expected = UserIdNotFoundException.class)
    public void testExceptionUser() throws Exception {
        Network nw = new Network();
        nw.addUser(2, "A", 20);
        nw.uploadVideo(2, 101, "tech");
        nw.recommendNthUp(1, 1);
    }
    
    @Test(expected = InvalidRankException.class)
    public void testExceptionRank() throws Exception {
        Network nw = new Network();
        nw.addUser(1, "A", 20);
        nw.uploadVideo(1, 101, "tech");
        nw.recommendNthUp(1, 0);
    }
    
    @Test(expected = NoVideoUploadedException.class)
    public void testExceptionNoVideo() throws Exception {
        Network nw = new Network();
        nw.addUser(1, "A", 20);
        nw.addUser(2, "B", 20);
        nw.recommendNthUp(1, 1);
    }
    
    @Test(expected = ColdStartUserException.class)
    public void testExceptionColdStart() throws Exception {
        Network nw = new Network();
        nw.addUser(1, "A", 20);
        nw.addUser(2, "B", 20);
        nw.uploadVideo(2, 101, "tech");
        nw.recommendNthUp(1, 2);
    }
    
    @Test
    public void testRecommendNthUp() throws Exception {
        Network nw = new Network();
        nw.addUser(1, "User1", 20);
        nw.addUser(2, "CandidateA", 22);
        nw.addUser(3, "CandidateB", 25);
        nw.addUser(4, "CandidateC", 30);
        nw.addUser(5, "AlreadyFollowed", 20);
        
        nw.uploadVideo(2, 101, "tech");
        nw.uploadVideo(3, 102, "tech");
        nw.uploadVideo(4, 103, "music");
        
        nw.watchVideo(1, 101);
        nw.watchVideo(1, 102);
        
        nw.followUser(1, 5);
        UserInterface[] usersBefore = nw.getUsers();
        int rank = 2; // 测试第 2 名
        int resultId = nw.recommendNthUp(1, rank);
        
        List<UserInterface> candidates = new ArrayList<>();
        for (UserInterface u : usersBefore) {
            if (u.getId() != 1 && !nw.getUser(1).isFollowing(u)) {
                candidates.add(u);
            }
        }
        
        int totalV = 3;
        candidates.sort((u1, u2) -> {
            long s1 = nw.getUser(1).computeUpScore(u1, totalV);
            long s2 = nw.getUser(1).computeUpScore(u2, totalV);
            if (s1 != s2) return Long.compare(s2, s1);
            return Integer.compare(u1.getId(), u2.getId());
        });
        
        Assert.assertEquals(candidates.get(rank - 1).getId(), resultId);
        
        UserInterface[] usersAfter = nw.getUsers();
        Assert.assertEquals(usersBefore.length, usersAfter.length);
        for (int i = 0; i < usersBefore.length; i++) {
            Assert.assertTrue(((User) usersBefore[i]).strictEquals(usersAfter[i]));
        }
    }
}