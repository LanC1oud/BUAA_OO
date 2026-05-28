//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//public class CleanSpamCommentsTest {
//    private Network nw;
//
//    @Before
//    public void setUp() {
//        nw = new Network();
//    }
//
//    @Test
//    public void testCleanSpamComments() throws Exception {
//        nw.addUser(1, "UP", 20);
//        nw.addUser(2, "User1", 20);
//        nw.addUser(3, "User2", 20);
//        nw.uploadVideo(1, 101, "tech");
//        nw.watchVideo(2, 101);
//        nw.watchVideo(3, 101);
//
//        nw.sendComment(2, 101, 1, "apple apple apple"); // apple * 3
//        nw.sendComment(3, 101, 2, "banana");            // no apple
//        nw.sendComment(2, 101, 3, "apple apple");       // apple * 2
//        nw.sendComment(3, 101, 4, "aaaaa");
//
//        int[] res = nw.cleanSpamComments(101, "apple");
//
//        Assert.assertEquals(2, res[0]);
//        Assert.assertEquals(3, res[1]);
//
//        int[] remainIds = ((Video)nw.getVideo(101)).getCommentIds();
//        Assert.assertEquals(2, remainIds.length);
//        Assert.assertEquals(2, remainIds[0]);
//        Assert.assertEquals(4, remainIds[1]);
//
//        int[] resOverlap = nw.cleanSpamComments(101, "aa");
//        Assert.assertEquals(1, resOverlap[0]);
//        Assert.assertEquals(4, resOverlap[1]);
//
//        nw.uploadVideo(1, 102, "tech");
//        Video v1 = (Video) nw.getVideo(101);
//        Video v2 = (Video) nw.getVideo(102);
//
//        Assert.assertNotEquals(v1.getId(), v2.getId());
//        Assert.assertEquals(v1.getUploaderId(), v2.getUploaderId());
//        Assert.assertEquals(v1.getType(), v2.getType());
//        Assert.assertNotEquals(v1.getPlayCount(), v2.getPlayCount());
//        Assert.assertEquals(v1.getLikes(), v2.getLikes());
//        Assert.assertEquals(v1.getForwardCount(), v2.getForwardCount());
//        Assert.assertEquals(v1.getCoins(), v2.getCoins());
//        Assert.assertNotEquals(v1.getCommentIds(), v2.getCommentIds());
//        Assert.assertNotEquals(v1.getCommentContents(), v2.getCommentContents());
//        nw.sendComment(2, 102, 2, "banana");
//
//        String[] contents1 = v1.getCommentContents();
//        String[] contents2 = v2.getCommentContents();
//        int[] ids1 = v1.getCommentIds();
//        int[] ids2 = v2.getCommentIds();
//        int minLen = Math.min(contents1.length, contents2.length);
//        for (int i = 0; i < minLen; i++) {
//            Assert.assertEquals(ids1[i], ids2[i]);
//            Assert.assertEquals(contents1[i], contents2[i]);
//        }
//    }
//
//
//}
//
