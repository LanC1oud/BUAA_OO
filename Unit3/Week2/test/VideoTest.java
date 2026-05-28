//import org.junit.Assert;
//import org.junit.Test;
//
//public class VideoTest {
//    @Test
//    public void testVideoConstructorAndGetters() {
//        Video video = new Video(101, 1);
//        Assert.assertEquals(101, video.getId());
//        Assert.assertEquals(1, video.getUploaderId());
//    }
//
//    @Test
//    public void testVideoEquals() {
//        Video v1 = new Video(100, 1);
//        Video v2 = new Video(100, 1); // ID 和 Uploader 都相同
//        Video v3 = new Video(101, 1); // ID 不同a
//        Video v4 = new Video(100, 2); // Uploader 不同
//
//        Assert.assertEquals(v1, v2);
//        Assert.assertNotEquals(v1, v3);
//        Assert.assertEquals(v1, v4);
//        Assert.assertNotEquals(null, v1);
//    }
//}