import com.oocourse.spec1.main.UserInterface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class QueryMutualFollowingSumTest {
    private Network net1;
    private Network net2;
    
    @Before
    public void setUp() {
        net1 = new Network();
        net2 = new Network();
    }
    
    private void compare(Network n1, Network n2) {
        UserInterface[] u1 = n1.getUsers();
        UserInterface[] u2 = n2.getUsers();
        Assert.assertEquals(u1.length, u2.length);
        for (UserInterface user1 : u1) {
            UserInterface user2 = n2.getUser(user1.getId());
            Assert.assertNotNull(user2);
            Assert.assertTrue(((User) user1).strictEquals(user2));
        }
    }
    
    private int getJmlTruth(Network net) {
        UserInterface[] users = net.getUsers();
        int sum = 0;
        for (int i = 0; i < users.length; i++) {
            for (int j = i + 1; j < users.length; j++) {
                if (users[i].isFollowing(users[j]) && users[j].isFollowing(users[i])) {
                    sum++;
                }
            }
        }
        return sum;
    }
    
    @Test
    public void simpleTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            net1.addUser(i, "u" + i, 10*i);
            net2.addUser(i, "u" + i, 10*i);
        }
        
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                if (i != j) {
                    net1.followUser(i,j);
                    net2.followUser(i,j);
                }
            }
        }
        Assert.assertEquals(45, getJmlTruth(net1));
    }
    
    @Test
    public void followAndUnfollowTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            net1.addUser(i, "u" + i, 10*i);
            net2.addUser(i, "u" + i, 10*i);
        }
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                if (i != j) {
                    net1.followUser(i,j);
                    net2.followUser(i,j);
                }
            }
        }
        Assert.assertEquals(net1.queryMutualFollowingSum(),getJmlTruth(net1));
        net1.unfollowUser(1,2);
        net1.unfollowUser(2,3);
        Assert.assertEquals(net1.queryMutualFollowingSum(),getJmlTruth(net1));
        net1.unfollowUser(2,1);
        net1.unfollowUser(3,2);
        Assert.assertEquals(net1.queryMutualFollowingSum(),getJmlTruth(net1));
        net1.followUser(2,1);
        net1.followUser(3,2);
        UserInterface[] u11 = net1.getUsers();
        UserInterface[] u21 = net2.getUsers();
        Assert.assertEquals(u11.length, u21.length);
        net1.followUser(1,2);
        net2.unfollowUser(2,3);
        Assert.assertEquals(net1.queryMutualFollowingSum(),getJmlTruth(net1));
        UserInterface[] u12 = net1.getUsers();
        UserInterface[] u22 = net2.getUsers();
        Assert.assertEquals(u12.length, u22.length);
        for (UserInterface user1 : u12) {
            UserInterface user2 = net2.getUser(user1.getId());
            Assert.assertNotNull(user2);
            Assert.assertTrue(((User) user1).strictEquals(user2));
        }
        net1.followUser(2,3);
        net2.followUser(2,3);
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                if (i != j) {
                    net1.unfollowUser(i,j);
                    Assert.assertEquals(net1.queryMutualFollowingSum(),getJmlTruth(net1));
                }
            }
        }
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                if (i != j) {
                    net1.followUser(i,j);
                }
            }
        }
        for (UserInterface user1 : u12) {
            UserInterface user2 = net2.getUser(user1.getId());
            Assert.assertNotNull(user2);
            Assert.assertTrue(((User) user1).strictEquals(user2));
        }
        Assert.assertEquals(net1.queryMutualFollowingSum(),getJmlTruth(net1));
    }
    
    @Test
    public void emptyTest() {
        Assert.assertEquals(net1.queryMutualFollowingSum(),getJmlTruth(net1));
    }
    
    @Test
    public void testWithVideoInterference() throws Exception {
        net1.addUser(1,"u1",35);
        net2.addUser(1,"u1",35);
        
        net1.uploadVideo(1, 999);
        net2.uploadVideo(1, 999);
        
        Assert.assertEquals(getJmlTruth(net1), net1.queryMutualFollowingSum());
        compare(net1, net2);
    }
    
    @Test
    public void testPureness() throws Exception {
        for (int i = 1; i <= 10; i++) {
            net1.addUser(i, "u" + i, 10*i);
            net2.addUser(i, "u" + i, 10*i);
        }
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                if (i != j) {
                    net1.followUser(i,j);
                    net2.followUser(i,j);
                }
            }
        }
        for (int i = 0; i < 50; i++) {
            int res = net1.queryMutualFollowingSum();
            Assert.assertEquals(45, res);
        }
        compare(net1, net2);
    }
    
    @Test
    public void testDisconnectedIslands() throws Exception {
        Network net3 = new Network();
        net3.addUser(1, "u1", 20);
        net3.addUser(2, "u2", 20);
        net3.addUser(3, "u3", 20);
        net3.addUser(4, "u4", 20);
        
        net3.followUser(1, 2);
        net3.followUser(2, 1);
        net3.followUser(3, 4);
        net3.followUser(4, 3);
        
        Assert.assertEquals(2, net3.queryMutualFollowingSum());
    }
}