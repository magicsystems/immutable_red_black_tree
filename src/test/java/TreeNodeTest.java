import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TreeNodeTest {

    @Test
    public void test0() {
        TreeNode treeNode = TreeNode.empty().remove(5).add(5, "R");
        verity(treeNode, 1);
        assertEquals("R", treeNode.get(5));
        treeNode = treeNode.remove(5);
        verity(treeNode, 0);
    }

    @Test
    public void test1() {
        TreeNode treeNode = TreeNode.single(8, "A").add(6, "B").add(7, "C").remove(5);
        verity(treeNode, 3);
        assertEquals("A", treeNode.get(8));
        assertEquals("C", treeNode.get(7));
        assertEquals("B", treeNode.get(6));
    }

    @Test
    public void test2() {
        TreeNode treeNode = TreeNode.empty().add(8, "A").add(6, "B").add(7, "C").remove(6).add(8, "E");
        verity(treeNode, 2);
        assertEquals("E", treeNode.get(8));
        assertEquals("C", treeNode.get(7));
        assertEquals(null, treeNode.get(6));
    }

    @Test
    public void test3() {
        TreeNode treeNode = TreeNode.single(8, "A").add(6, "B").remove(11)
                .add(7, "C").remove(5).add(5, "D").add(9, "C").remove(9).remove(8).add(4, "E").add(11, "F").add(10, "G").add(9, "C");
        verity(treeNode, 7);
        assertEquals("E", treeNode.get(4));
        assertEquals("D", treeNode.get(5));
        assertEquals("B", treeNode.get(6));
        assertEquals("C", treeNode.get(7));
        assertEquals("C", treeNode.get(9));
        assertEquals("G", treeNode.get(10));
        assertEquals("F", treeNode.get(11));
    }

    @Test
    public void test6() {
        TreeNode treeNode = TreeNode.empty().add(8, "A").add(10, "Q").add(7, "C")
                .add(9, "D").add(13, "Y")
                .add(34, "S").remove(9);
        verity(treeNode);
    }

    @Test
    public void test7() {
        TreeNode treeNode = TreeNode.empty().add(8, "A").add(10, "Q").add(7, "C")
                .add(9, "D").add(13, "Y")
                .add(34, "S").add(19, "V").remove(8);
        verity(treeNode);
    }

    @Test
    public void test8() {
        TreeNode treeNode = TreeNode.empty().add(8, "A")
                .add(34, "S").add(17, "R").add(50, "e").add(17, "C").remove(17);
        verity(treeNode);
    }

    @Test
    public void testRandom() {
        int numberOfNodes = 2000;
        int numberOfUniqueKeys = 500;
        TreeNode treeNode = TreeNode.empty();
        for (int i = 0; i < numberOfNodes; i++) {
            int key = (int) (Math.random() * numberOfUniqueKeys);
            if (i % 3 == 0) {
                treeNode = treeNode.remove(key);
            } else {
                treeNode = treeNode.add(key, "VALUE");
            }
            verity(treeNode);
        }
    }


    private static void verity(TreeNode treeNode, int expectedSize) {
        assertEquals(expectedSize, treeNode.size());
        verity(treeNode);
    }

    private static void verity(TreeNode treeNode) {
        verifyRootIsBlack(treeNode);
        verifyBlackNumberInPath(treeNode);
        verifyRedChildrenAreRed(treeNode);
    }

    private static void verifyRootIsBlack(TreeNode treeNode) {
        assertTrue(TreeNode.isBlack(treeNode));
    }

    private static void verifyRedChildrenAreRed(TreeNode treeNode) {
        if (treeNode == null) return;
        if (TreeNode.isRed(treeNode)) {
            assertFalse(TreeNode.isRed(treeNode.left));
            assertFalse((TreeNode.isRed(treeNode.right)));
        }
    }

    private static void verifyBlackNumberInPath(TreeNode treeNode) {
        verifyBlackNumberInPath(treeNode, 0, -1);
    }

    private static int verifyBlackNumberInPath(TreeNode treeNode, int blackCount, int pathBlackCount) {
        if (TreeNode.isBlack(treeNode)) {
            blackCount++;
        }
        if (treeNode == null) {
            if (pathBlackCount == -1) {
                pathBlackCount = blackCount;
            } else {
                assertEquals(blackCount, pathBlackCount);
            }
            return pathBlackCount;
        }
        pathBlackCount = verifyBlackNumberInPath(treeNode.left, blackCount, pathBlackCount);
        pathBlackCount = verifyBlackNumberInPath(treeNode.right, blackCount, pathBlackCount);
        return pathBlackCount;
    }
}

