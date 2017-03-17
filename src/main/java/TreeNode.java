/**
 * Implementation of immutable red-black tree. Assume that keys are "int" and values are "String".
 * Main idea - if we have parents reference in immutable node, we have to recreate every node on each update.
 * Lets forget about parents, so recreate only O(ln) elements and try to do red-black recovery stuff in
 * recursive calls on each changed level.
 * <p/>
 * Thanks {@link java.util.TreeMap} and "Introduction to Algorithms" aka "Cormen" for help.
 * <p/>
 * Example:
 * <p/>
 * TreeNode treeNode = TreeNode.single(8, "A").add(6, "B").add(7, "C").remove(6);
 * String value = treeNode.get(7);
 */
public class TreeNode {
    private static final boolean RED = false;
    private static final boolean BLACK = true;

    private final int key;
    private final String value;
    final TreeNode left, right;
    private final int size;
    private final boolean color;
    //maybe not best implementation of "super black" / "red black" node concept
    private final boolean superBlack;

    private TreeNode(int key, String value, TreeNode left, TreeNode right,
                     int size, boolean color) {
        this.key = key;
        this.value = value;
        this.left = left;
        this.right = right;
        this.size = size;
        this.color = color;
        this.superBlack = false;
    }

    private TreeNode(int key, String value, TreeNode left, TreeNode right,
                     int size, boolean color, boolean superBlack) {
        this.key = key;
        this.value = value;
        this.left = left;
        this.right = right;
        this.size = size;
        this.color = color;
        this.superBlack = superBlack;
    }

    /**
     * Factory method for creating empty node
     *
     * @return empty node
     */
    public static TreeNode empty() {
        return new TreeNode(0, null, null, null, 0, BLACK);
    }

    /**
     * Factory method for creating Node with one element
     *
     * @param key   to be added into tree
     * @param value mapped with specified key
     * @return new tree with one element
     */
    public static TreeNode single(int key, String value) {
        return empty().add(key, value);
    }

    /**
     * Adds the specified value with the specified key to this tree and
     * return new root node with applied changes. If specified key already exists its value
     * will be overridden by new one
     *
     * @param key   to be added into tree
     * @param value mapped with specified key
     * @return new node which is sum of previous node and new element
     */
    public TreeNode add(int key, String value) {
        if (size == 0) {
            return new TreeNode(key, value, null, null, 1, BLACK);
        } else {
            TreeNode node = addNode(key, value);
            if (isRed(node)) {
                return setColor(node, BLACK);
            }
            return node;
        }
    }

    /**
     * Removes the specified key from tree and return new root node
     * with applied changes
     *
     * @param key to be removed from tree
     * @return new node with removed
     */
    public TreeNode remove(int key) {
        if (size == 0) {
            return this;
        } else {
            TreeNode node = removeNode(key);
            if (node == null) {
                return empty();
            } else if (isRed(node)) {
                return setColor(node, BLACK);
            }
            return node;
        }
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this tree contains no mapping for the key.
     *
     * @param key whose value is to be returned
     * @return value mapped with the key
     */
    public String get(int key) {
        if (this.key == key) {
            return value;
        }
        if (this.key > key && left != null) {
            return left.get(key);
        } else if (this.key < key && right != null) {
            return right.get(key);
        }
        return null;
    }

    /**
     * Returns number of elements in current tree
     *
     * @return number of elements
     */
    public int size() {
        return size;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (size() == 0) {
            stringBuilder.append("empty");
        } else {
            stringBuilder.append(value).append(key).append(color ? "black" : "red").append("(").
                    append(left != null ? left.toString() : "null").append("  ").
                    append(right != null ? right.toString() : "null").append(")");
        }
        return stringBuilder.toString();
    }

    private TreeNode addNode(int key, String value) {
        if (this.key > key) {
            return fixAfterInsertion(addIntoChildNode(left, key, value), right);
        } else if (this.key < key) {
            return fixAfterInsertion(left, addIntoChildNode(right, key, value));
        } else {
            return new TreeNode(key, value, left, right, size, color);
        }
    }

    private TreeNode removeNode(int key) {
        if (this.key > key) {
            return fixAfterDeletion(removeNode(left, key), right);
        } else if (this.key < key) {
            return fixAfterDeletion(left, removeNode(right, key));
        } else {
            if (left == null && right == null) {
                return null;
            } else if (left == null || right == null) {
                TreeNode notNullChild = left != null ? left : right;
                //if removed element is black, we need to do something
                return copyWithSuperBlack(notNullChild, notNullChild.left, notNullChild.right, isBlack(this));
            } else {
                //replace removed element with successor - smallest element from the right subtree
                TreeNode successor = right;
                while (successor.left != null) {
                    successor = successor.left;
                }
                //need to check red-black for this level with "replaced" element
                return copyWithColor(successor, this.left, this.right, this.color).
                        fixAfterDeletion(this.left, removeNode(right, successor.key));
            }
        }
    }

    private TreeNode fixAfterInsertion(TreeNode newLeft, TreeNode newRight) {
        // new left sub tree - it contains new element
        if (newLeft != left) {
            //no violation on this level
            if (isBlack(left)) {
                return copy(this, newLeft, newRight);
            }
            //check Case 1
            if (isRed(newRight)) {
                TreeNode left = setColor(newLeft, BLACK);
                TreeNode right = setColor(newRight, BLACK);
                return copyWithColor(this, left, right, RED);
            } else {
                //check Case 2
                TreeNode currentLeft = newLeft;
                if (isRed(rightOf(newLeft))) {
                    currentLeft = rotateLeft(rightOf(newLeft), newLeft);
                }
                //check Case 3
                currentLeft = setColor(currentLeft, BLACK);
                TreeNode current = setColor(this, RED);
                return rotateRight(currentLeft, current);
            }
        } else {  //symmetric
            if (isBlack(right)) {
                return copy(this, newLeft, newRight);
            }
            if (isRed(newLeft)) {
                TreeNode left = setColor(newLeft, BLACK);
                TreeNode right = setColor(newRight, BLACK);
                return copyWithColor(this, left, right, RED);
            } else {
                TreeNode currentRight = newRight;
                if (isRed(leftOf(newRight))) {
                    currentRight = rotateRight(leftOf(newRight), newRight);
                }
                currentRight = setColor(currentRight, BLACK);
                TreeNode current = setColor(this, RED);
                return rotateLeft(currentRight, current);
            }
        }
    }

    private TreeNode fixAfterDeletion(TreeNode newLeft, TreeNode newRight) {
        // new left sub tree - it contained removed element
        if (newLeft != left) {
            if (!isSuperBlack(newLeft) && (newLeft != null || isRed(left))) {
                // new element not "super black" - removed element not black or it could be fixed on previous steps
                return copy(this, newLeft, newRight);
            }
            //paint "red black" into black
            if (isRed(newLeft) && isSuperBlack(newLeft)) {
                return copy(this, setColor(newLeft, BLACK), newRight);
            }
            if (isRed(newRight)) {
                //check Case 1
                TreeNode parent = rotateLeft(setColor(newRight, BLACK), setColor(this, RED));
                newLeft = leftOf(parent).fixAfterDeletion(newLeft, rightOf(leftOf(parent)));
                return parent.fixAfterDeletion(newLeft, rightOf(parent));
            }
            if (isBlack(leftOf(newRight)) && isBlack(rightOf(newRight))) {
                //check Case 2, we still need "super black" flag
                return copyWithSuperBlack(this, newLeft, setColor(newRight, RED), true);
            } else {
                if (isBlack(rightOf(newRight))) {
                    //check Case 3
                    newRight = copyWithColor(newRight, setColor(leftOf(newRight), BLACK), rightOf(newRight), RED);
                    newRight = rotateRight(leftOf(newRight), newRight);
                }
                //check Case 4, "super black" case resolved - it's no longer needed
                newRight = copyWithColor(newRight, leftOf(newRight), setColor(rightOf(newRight), BLACK), colorOf(this));
                return rotateLeft(newRight, copyWithColor(this, newLeft, newRight, BLACK));
            }
        } else if (newRight != right) { //symmetric
            if (!isSuperBlack(newRight) && (newRight != null || isRed(right))) {
                return copy(this, newLeft, newRight);
            }
            if (isRed(newRight) && isSuperBlack(newRight)) {
                return copy(this, newLeft, setColor(newRight, BLACK));
            }
            if (isRed(newLeft)) {
                TreeNode parent = rotateRight(setColor(newLeft, BLACK), setColor(this, RED));
                newRight = rightOf(parent).fixAfterDeletion(leftOf(rightOf(parent)), newRight);
                return parent.fixAfterDeletion(leftOf(parent), newRight);
            }
            if (isBlack(leftOf(newLeft)) && isBlack(rightOf(newLeft))) {
                return copyWithSuperBlack(this, setColor(newLeft, RED), newRight, true);
            } else {
                if (isBlack(leftOf(newLeft))) {
                    newLeft = copyWithColor(newLeft, leftOf(newLeft), setColor(rightOf(newLeft), BLACK), RED);
                    newLeft = rotateLeft(rightOf(newLeft), newLeft);
                }
                newLeft = copyWithColor(newLeft, setColor(leftOf(newLeft), BLACK), rightOf(newLeft), colorOf(this));
                return rotateRight(newLeft, copyWithColor(this, newLeft, newRight, BLACK));
            }
        }
        return this;
    }

    // ___________________________ Static util functions _____________________________

    private static TreeNode addIntoChildNode(TreeNode node, int key, String value) {
        return node == null ? newNode(key, value) : node.addNode(key, value);
    }

    private static TreeNode removeNode(TreeNode node, int key) {
        if (node == null) {
            return null;
        } else {
            return node.removeNode(key);
        }
    }

    private static TreeNode newNode(int key, String value) {
        return new TreeNode(key, value, null, null, 1, RED);
    }

    private static Boolean colorOf(TreeNode node) {
        return node == null ? BLACK : node.color;
    }

    private static TreeNode leftOf(TreeNode node) {
        return node == null ? null : node.left;
    }

    private static TreeNode rightOf(TreeNode node) {
        return node == null ? null : node.right;
    }

    private static int sizeOf(TreeNode node) {
        return node != null ? node.size : 0;
    }

    static boolean isBlack(TreeNode node) {
        return colorOf(node);
    }

    static boolean isRed(TreeNode node) {
        return !colorOf(node);
    }

    private static boolean isSuperBlack(TreeNode node) {
        return node != null && node.superBlack;
    }

    private static TreeNode rotateLeft(TreeNode child, TreeNode parent) {
        TreeNode newChild = copy(parent, leftOf(parent), leftOf(child));
        return copy(child, newChild, rightOf(child));
    }

    private static TreeNode rotateRight(TreeNode child, TreeNode parent) {
        TreeNode newChild = copy(parent, rightOf(child), rightOf(parent));
        return copy(child, leftOf(child), newChild);
    }

    private static TreeNode setColor(TreeNode treeNode, boolean color) {
        if (treeNode == null) {
            return null;
        }
        return new TreeNode(treeNode.key, treeNode.value, treeNode.left, treeNode.right, treeNode.size, color);
    }

    private static TreeNode copyWithColor(TreeNode src, TreeNode newLeft, TreeNode newRight, boolean color) {
        if (src == null) {
            return null;
        }
        return new TreeNode(src.key, src.value, newLeft, newRight, sizeOf(newLeft) + sizeOf(newRight) + 1, color);
    }

    private static TreeNode copy(TreeNode src, TreeNode newLeft, TreeNode newRight) {
        return copyWithColor(src, newLeft, newRight, colorOf(src));
    }

    private static TreeNode copyWithSuperBlack(TreeNode src, TreeNode newLeft, TreeNode newRight, boolean color, boolean superBlack) {
        if (src == null) {
            return null;
        }
        return new TreeNode(src.key, src.value, newLeft, newRight, sizeOf(newLeft) + sizeOf(newRight) + 1, color, superBlack);
    }

    private static TreeNode copyWithSuperBlack(TreeNode src, TreeNode newLeft, TreeNode newRight, boolean superBlack) {
        return copyWithSuperBlack(src, newLeft, newRight, colorOf(src), superBlack);
    }

    // ____________________________________________________________________________________________

}


