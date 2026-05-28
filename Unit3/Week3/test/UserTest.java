//import org.junit.Assert;
//import org.junit.Test;
//
//import java.util.List;
//
//public class UserTest {
//
//    @Test
//    public void testQueryAgeRatio() {
//        User up = new User(1, "UP主", 25);
//
//        double[] emptyRatios = up.queryAgeRatio();
//        for (double r : emptyRatios) {
//            Assert.assertEquals(0.0, r, 0.0001);
//        }
//
//        User f1 = new User(2, "少年", 16);  // 第一段: <=16
//        User f2 = new User(3, "青年1", 17); // 第二段: 17-30
//        User f3 = new User(4, "青年2", 30); // 第二段: 17-30
//        User f4 = new User(5, "中年", 35);  // 第三段: 31-45
//        User f5 = new User(6, "老年", 50);  // 第四段: >=46
//
//        up.addFollower(f1);
//        up.addFollower(f2);
//        up.addFollower(f3);
//        up.addFollower(f4);
//        up.addFollower(f5);
//
//        double[] ratios = up.queryAgeRatio();
//        Assert.assertEquals(0.2, ratios[0], 0.0001);
//        Assert.assertEquals(0.4, ratios[1], 0.0001);
//        Assert.assertEquals(0.2, ratios[2], 0.0001);
//        Assert.assertEquals(0.2, ratios[3], 0.0001);
//    }
//
//    @Test
//    public void testReceivedVideosOrderAndLimit() {
//        User user = new User(1, "Alice", 20);
//
//        for (int i = 101; i <= 106; i++) {
//            user.receiveVideo(i);
//        }
//
//        List<Integer> result = user.queryReceivedUnwatchedVideos();
//
//        Assert.assertEquals(5, result.size());
//
//        Assert.assertEquals(Integer.valueOf(106), result.get(0));
//        Assert.assertEquals(Integer.valueOf(105), result.get(1));
//        Assert.assertEquals(Integer.valueOf(104), result.get(2));
//        Assert.assertEquals(Integer.valueOf(103), result.get(3));
//        Assert.assertEquals(Integer.valueOf(102), result.get(4));
//
//        user.watchVideo(106);
//        List<Integer> newResult = user.queryReceivedUnwatchedVideos();
//        Assert.assertEquals(Integer.valueOf(105), newResult.get(0));
//        Assert.assertEquals(5, newResult.size());
//    }
//
//    @Test
//    public void testFollowLogic() {
//        User u1 = new User(1, "A", 20);
//        User u2 = new User(2, "B", 20);
//
//        Assert.assertFalse(u1.isFollowing(u2));
//        u1.addFollowing(u2);
//        Assert.assertTrue(u1.isFollowing(u2));
//
//        u1.removeFollowing(2);
//        Assert.assertFalse(u1.isFollowing(u2));
//    }
//}