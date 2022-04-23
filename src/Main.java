import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
//        var g = new Graph("instances/test.txt");
        var g = new Graph("instances/Range_100/ins_500_1.txt");
        Random random = new Random(4);
        int numE = 5000;
//        test_super(g, random);
//        test_sub(g, random);
//        test_update(g, random);
        test_sub_edge_version(g, random, numE);
        test_super_edge_version(g, random, numE);
    }

    static void test_sub_edge_version(Graph g, Random random, int numE){
        System.out.println("\n测试子图：");
        long u_time = 0;
        long k_time = 0;
        for(int run = 0; run<1000; run++) {
            for(var e: g.allEdges){
                g.unmuteEdge(e);
            }
            var remove_edges_set = new HashSet<Graph.Edge>();
            var edges = g.allUnmuteEdges();
            while (remove_edges_set.size() < numE) {
                remove_edges_set.add(edges.get(random.nextInt(edges.size())));
            }

            var ori_tree = g.spanningTree_k();

            Collections.shuffle(g.allEdges, random);
            var remove_edges = new ArrayList<>(remove_edges_set);
            long start = System.currentTimeMillis();
            var update_tree = g.spanningTree_of_sub(ori_tree, remove_edges);
            u_time += System.currentTimeMillis() - start;

            for (var e : remove_edges) {
                g.muteEdge(e);
            }
            Collections.shuffle(g.allEdges, random);
            start = System.currentTimeMillis();
            var k_tree = g.spanningTree_k();
            k_time += System.currentTimeMillis() - start;
            if (Math.abs(update_tree.cost - k_tree.cost) > 0.0001) {
                throw new Error();
            }
        }
        System.out.println("更新算法耗时：" + u_time);
        System.out.println("克鲁斯卡尔耗时：" + k_time);
    }

    static void test_super_edge_version(Graph g, Random random, int numE){
        System.out.println("\n测试超图：");
        long u_time = 0;
        long k_time = 0;
        for(int run = 0; run<1000; ++run){
            for(var e: g.allEdges){
                g.unmuteEdge(e);
            }
            var add_edges_set = new HashSet<Graph.Edge>();
            var edges = g.allUnmuteEdges();
            while (add_edges_set.size() < numE) {
                add_edges_set.add(edges.get(random.nextInt(edges.size())));
            }
            for(var e : add_edges_set){
                g.muteEdge(e);
            }
            var ori_tree = g.spanningTree_k();

            Collections.shuffle(g.allEdges, random);
            var add_edges = new ArrayList<>(add_edges_set);
            long start = System.currentTimeMillis();
            var update_tree = g.spanningTree_of_super(ori_tree, add_edges);
            u_time += System.currentTimeMillis() - start;

            for(var e : add_edges_set){
                g.unmuteEdge(e);
            }
            Collections.shuffle(g.allEdges, random);
            start = System.currentTimeMillis();
            var k_tree = g.spanningTree_k();
            k_time += System.currentTimeMillis() - start;
            if (Math.abs(update_tree.cost - k_tree.cost) > 0.0001) {
                throw new Error();
            }
        }
        System.out.println("更新算法耗时：" + u_time);
        System.out.println("克鲁斯卡尔耗时：" + k_time);
    }
}
