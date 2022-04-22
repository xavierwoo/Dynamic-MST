import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
//        var g = new Graph("instances/test.txt");
        var g = new Graph("instances/Range_100/ins_500_2.txt");
        Random random = new Random(4);
//        test_super(g, random);
//        test_sub(g, random);
//        test_update(g, random);
        test_sub_edge_version(g, random);
    }

    static void test_sub_edge_version(Graph g, Random random){
        System.out.println("\n测试子图：");
        long u_time = 0;
        long k_time = 0;
        for(int run = 0; run<100; run++) {
            for(var e: g.allEdges){
                g.unmuteEdge(e);
            }
            int numE = 10;
            var remove_edges_set = new HashSet<Graph.Edge>();
            var edges = g.allUnmuteEdges();
            while (remove_edges_set.size() < numE) {
                remove_edges_set.add(edges.get(random.nextInt(edges.size())));
            }

            var ori_tree = g.spanningTree_kruskal();

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

    static void test_super(Graph g, Random random) throws IOException {
        System.out.println("\n测试超图");

        long u_time = 0;
        long k_time = 0;
        for(int i=0; i<1; ++i) {
            for (int muteV = 0; muteV < g.vertices.size(); ++muteV) {

                g.muteVertex(muteV);
                var ori_tree = g.spanningTree_kruskal();
                //System.out.println(ori_tree.cost);

                g.unmuteVertex(muteV);
                Collections.shuffle(g.allEdges, random);
                long start = System.currentTimeMillis();
                var update_tree = g.spanningTree_of_super(ori_tree, g.vertices.get(muteV));
                u_time += System.currentTimeMillis() - start;

                Collections.shuffle(g.allEdges, random);
                start = System.currentTimeMillis();
                var tree = g.spanningTree_kruskal();
                k_time += System.currentTimeMillis() - start;
                if (Math.abs(update_tree.cost - tree.cost) > 0.0001) {
                    throw new Error();
                }
            }
        }
        System.out.println("更新算法耗时：" + u_time);
        System.out.println("克鲁斯卡尔耗时：" + k_time);
    }


    static void test_sub(Graph g, Random random) throws IOException {
        System.out.println("\n测试子图");
        long u_time = 0;
        long k_time = 0;
        for(int i=0; i<1; ++i) {
            for (int muteV = 0; muteV < g.vertices.size(); ++muteV) {

                var ori_tree = g.spanningTree_kruskal();
                //System.out.println(ori_tree.cost);
                g.muteVertex(muteV);

                Collections.shuffle(g.allEdges, random);
                long start = System.currentTimeMillis();
                var update_tree = g.spanningTree_of_sub(ori_tree, g.vertices.get(muteV));
                u_time += System.currentTimeMillis() - start;

                Collections.shuffle(g.allEdges, random);
                start = System.currentTimeMillis();
                var tree = g.spanningTree_kruskal();
                k_time += System.currentTimeMillis() - start;
                if (Math.abs(update_tree.cost - tree.cost) > 0.0001) {
                    throw new Error();
                }

                g.unmuteVertex(muteV);
            }
        }
        System.out.println("更新算法耗时：" + u_time);
        System.out.println("克鲁斯卡尔耗时：" + k_time);
    }

    static void test_update(Graph g, Random random){
        System.out.println("\n测试同时添加、删除顶点");
        long u_time = 0;
        long k_time = 0;
        for(int removeV=0; removeV < 1; ++removeV){
            for(int addV=0; addV < g.vertices.size(); ++addV){
                if(removeV == addV)continue;

                g.muteVertex(addV);
                var ori_tree = g.spanningTree_kruskal();

                g.unmuteVertex(addV);
                g.muteVertex(removeV);

                Collections.shuffle(g.allEdges, random);
                long start = System.currentTimeMillis();
                var update_tree = g.spanningTree_update(ori_tree, g.vertices.get(removeV), g.vertices.get(addV));
                u_time += System.currentTimeMillis() - start;

                Collections.shuffle(g.allEdges, random);
                start = System.currentTimeMillis();
                var tree = g.spanningTree_kruskal();
                k_time += System.currentTimeMillis() - start;
                if (Math.abs(update_tree.cost - tree.cost) > 0.0001) {
                    throw new Error();
                }
                g.unmuteVertex(removeV);
            }
        }
        System.out.println("更新算法耗时：" + u_time);
        System.out.println("克鲁斯卡尔耗时：" + k_time);
    }
}
