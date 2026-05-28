//import com.oocourse.spec1.exceptions.*;
//import com.oocourse.spec1.main.UserInterface;
//import com.oocourse.spec1.main.VideoInterface;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class NetworkTest {
//
//    private Network network;
//
//    @Before
//    public void setUp() {
//        network = new Network();
//    }
//
//    @Test
//    public void testAddUserNormal() throws Exception {
//        network.addUser(1, "Alice", 20);
//        Assert.assertTrue(network.containsUser(1));
//
//        UserInterface user = network.getUser(1);
//        Assert.assertNotNull(user);
//        Assert.assertEquals(1, user.getId());
//
//        Assert.assertEquals("Alice", user.getName());
//        Assert.assertEquals(20, user.getAge());
//    }
//
//    @Test(expected = EqualUserIdException.class)
//    public void testAddUserEqualUserId() throws Exception {
//        network.addUser(1, "Alice", 20);
//        network.addUser(1, "Bob", 22); // 重复ID，应抛出异常
//    }
//
//    @Test(expected = InvalidAgeException.class)
//    public void testAddUserInvalidAgeNegative() throws Exception {
//        network.addUser(2, "Bob", -1); // 年龄小于0
//    }
//
//    @Test(expected = InvalidAgeException.class)
//    public void testAddUserInvalidAgeTooLarge() throws Exception {
//        network.addUser(3, "Charlie", 111); // 年龄大于110
//    }
//
//    @Test
//    public void testFollowAndMutualSumNormal() throws Exception {
//        network.addUser(1, "Alice", 20);
//        network.addUser(2, "Bob", 22);
//        network.addUser(3, "Charlie", 25);
//
//        Assert.assertEquals(0, network.queryMutualFollowingSum());
//
//        network.followUser(1, 2); // 1 -> 2
//        Assert.assertEquals(0, network.queryMutualFollowingSum());
//
//        network.followUser(2, 1); // 1 <-> 2 (互粉)
//        Assert.assertEquals(1, network.queryMutualFollowingSum());
//
//        network.followUser(2, 3); // 2 -> 3
//        network.followUser(3, 2); // 2 <-> 3 (互粉)
//        Assert.assertEquals(2, network.queryMutualFollowingSum());
//
//        network.unfollowUser(1, 2); // 破坏 1 <-> 2
//        Assert.assertEquals(1, network.queryMutualFollowingSum());
//    }
//
//    @Test
//    public void testQueryMutualFollowingSum() throws Exception {
//        network.addUser(1, "Alice", 20);
//        network.addUser(2, "Bob", 22);
//        network.addUser(3, "Charlie", 25);
//        network.addUser(4, "Dave", 30);
//
//        Assert.assertEquals(0, network.queryMutualFollowingSum());
//
//        network.followUser(1, 2);
//        network.followUser(2, 3);
//        Assert.assertEquals(0, network.queryMutualFollowingSum());
//
//        network.followUser(2, 1);
//        Assert.assertEquals(1, network.queryMutualFollowingSum());
//
//        network.followUser(3, 4);
//        network.followUser(4, 3);
//        Assert.assertEquals(2, network.queryMutualFollowingSum());
//
//        network.followUser(1, 3);
//        network.followUser(3, 1);
//        Assert.assertEquals(3, network.queryMutualFollowingSum());
//
//        UserInterface[] usersBefore = network.getUsers();
//        int expectedCount = usersBefore.length;
//
//        Map<Integer, String> namesBefore = new HashMap<>();
//        Map<Integer, Integer> agesBefore = new HashMap<>();
//        Map<Integer, List<Integer>> followingsBefore = new HashMap<>();
//
//        for (UserInterface u : usersBefore) {
//            int id = u.getId();
//            namesBefore.put(id, u.getName());
//            agesBefore.put(id, u.getAge());
//
//            List<Integer> follows = new ArrayList<>();
//            for (UserInterface target : usersBefore) {
//                if (u.isFollowing(target)) {
//                    follows.add(target.getId());
//                }
//            }
//            followingsBefore.put(id, follows);
//        }
//
//        int expectedResult = 3;
//        int call1 = network.queryMutualFollowingSum();
//        int call2 = network.queryMutualFollowingSum();
//
//        Assert.assertEquals(expectedResult, call1);
//        Assert.assertEquals(call1, call2);
//
//        UserInterface[] usersAfter = network.getUsers();
//
//        Assert.assertEquals(expectedCount, usersAfter.length);
//
//        for (UserInterface u : usersAfter) {
//            int id = u.getId();
//
//            Assert.assertTrue(namesBefore.containsKey(id));
//
//            Assert.assertEquals(namesBefore.get(id), u.getName());
//            Assert.assertEquals((int) agesBefore.get(id), u.getAge());
//
//            List<Integer> oldFollows = followingsBefore.get(id);
//            for (UserInterface target : usersAfter) {
//                boolean wasFollowing = oldFollows.contains(target.getId());
//                boolean isFollowingNow = u.isFollowing(target);
//                Assert.assertEquals(wasFollowing, isFollowingNow);
//            }
//        }
//    }
//
//    @Test(expected = UserIdNotFoundException.class)
//    public void testFollowUserNotFound() throws Exception {
//        network.addUser(1, "Alice", 20);
//        network.followUser(1, 999); // 999不存在
//    }
//
//    @Test(expected = SelfSubscriptionException.class)
//    public void testFollowSelf() throws Exception {
//        network.addUser(1, "Alice", 20);
//        network.followUser(1, 1); // 不能关注自己
//    }
//
//    @Test(expected = DuplicateSubscriptionException.class)
//    public void testFollowDuplicate() throws Exception {
//        network.addUser(1, "Alice", 20);
//        network.addUser(2, "Bob", 22);
//        network.followUser(1, 2);
//        network.followUser(1, 2); // 重复关注
//    }
//
//    @Test(expected = FollowLinkNotFoundException.class)
//    public void testUnfollowLinkNotFound() throws Exception {
//        network.addUser(1, "Alice", 20);
//        network.addUser(2, "Bob", 22);
//        network.unfollowUser(1, 2); // 尚未关注就取关
//    }
//
//    @Test
//    public void testVideoUploadAndWatchNormal() throws Exception {
//        network.addUser(1, "Uploader", 25);
//        network.addUser(2, "Fan1", 20);
//        network.addUser(3, "Fan2", 22);
//
//        network.followUser(2, 1);
//        network.followUser(3, 1);
//
//        network.uploadVideo(1, 101);
//        Assert.assertTrue(network.containsVideo(101));
//
//        VideoInterface video = network.getVideo(101);
//        Assert.assertNotNull(video);
//        Assert.assertEquals(101, video.getId());
//
//        List<Integer> unwatchedFan1 = network.queryReceivedUnwatchedVideos(2);
//        Assert.assertTrue(unwatchedFan1.contains(101));
//
//        network.watchVideo(2, 101);
//
//        Assert.assertFalse(network.queryReceivedUnwatchedVideos(2).contains(101));
//
//        Assert.assertTrue(network.queryReceivedUnwatchedVideos(3).contains(101));
//    }
//
//    @Test(expected = EqualVideoIdException.class)
//    public void testUploadEqualVideoId() throws Exception {
//        network.addUser(1, "Uploader", 25);
//        network.uploadVideo(1, 101);
//        network.uploadVideo(1, 101); // 视频ID重复
//    }
//
//    @Test(expected = VideoIdNotFoundException.class)
//    public void testWatchVideoNotFound() throws Exception {
//        network.addUser(1, "Alice", 20);
//        network.watchVideo(1, 999); // 视频不存在
//    }
//
//    @Test
//    public void testQueryShortestPathNormal() throws Exception {
//        network.addUser(1, "A", 20);
//        network.addUser(2, "B", 20);
//        network.addUser(3, "C", 20);
//        network.addUser(4, "D", 20);
//        network.addUser(5, "E", 20);
//
//        network.followUser(1, 2);
//        network.followUser(2, 3);
//        network.followUser(3, 4);
//
//        network.followUser(1, 5);
//        network.followUser(5, 4);
//
//        Assert.assertEquals(0, network.queryShortestPath(1, 1));
//
//        Assert.assertEquals(1, network.queryShortestPath(1, 2));
//
//        Assert.assertEquals(2, network.queryShortestPath(1, 4));
//
//        int sumBefore = network.queryMutualFollowingSum();
//        network.queryShortestPath(1, 4);
//        Assert.assertEquals(sumBefore, network.queryMutualFollowingSum());
//    }
//
//    @Test(expected = UncessException.class)
//    public void testQueryShortestPathUnreachable() throws Exception {
//        network.addUser(1, "A", 20);
//        network.addUser(2, "B", 20);
//        network.addUser(3, "Island", 20); // 孤立节点
//
//        network.followUser(1, 2);
//
//        network.queryShortestPath(1, 3);
//    }
//
//    @Test
//    public void testPureMethods() throws Exception {
//        network.addUser(1, "A", 20);
//        network.addUser(2, "B", 20);
//        network.followUser(1, 2);
//
//        UserInterface[] usersBefore = network.getUsers();
//        int lengthBefore = usersBefore.length;
//
//        network.containsUser(1);
//        network.containsVideo(101);
//        network.getUser(1);
//        network.getVideo(101);
//
//        Assert.assertEquals(lengthBefore, network.getUsers().length);
//    }
//}