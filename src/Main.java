import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here

//        var g = new Graph("instances/test.txt");
        var g = new Graph("instances/Range_150/ins_100_1.txt");
        Random random = new Random(4);

        int removeE_num = 1;
        int addE_num = 1;
        test_update(g, random, removeE_num, addE_num);
    }

    static void test_update(Graph g, Random random, int removeE_num, int addE_num){
        System.out.println("\n测试更新：删除"+removeE_num+"条边，增加"+addE_num+"条边");
        long u_time = 0;
        long k_time = 0;
        var remove_set = new HashSet<Graph.Edge>(removeE_num);
        var add_set = new HashSet<Graph.Edge>(addE_num);

        Graph.SpanningTree prevTree = null;

        while (add_set.size() < addE_num) {
            var e = g.allEdges.get(random.nextInt(g.allEdges.size()));
            add_set.add(e);
        }

        for(var e : add_set){
            g.muteEdge(e);
        }

        for(int run=0; run < 1000; ++run) {
            remove_set.clear();
            add_set.clear();
            List<Graph.Edge> unmuteEdges = g.allEdges.stream().filter(e->!e.isMute).collect(Collectors.toList());
            while (remove_set.size() < removeE_num) {
                remove_set.add(unmuteEdges.get(random.nextInt(unmuteEdges.size())));
            }
            List<Graph.Edge> muteEdges = g.allEdges.stream().filter(e->e.isMute).collect(Collectors.toList());
            while (add_set.size() < addE_num) {
                add_set.add(muteEdges.get(random.nextInt(muteEdges.size())));
            }

            var removeE = new ArrayList<>(remove_set);
            var addE = new ArrayList<>(add_set);

            if(prevTree == null)prevTree = g.spanningTree_k();

            Collections.shuffle(g.allEdges);
            long start = System.currentTimeMillis();
            var update_tree = g.spanningTree_update(prevTree, removeE, addE);
            u_time += System.currentTimeMillis() - start;

            for (var e : removeE) {
                g.muteEdge(e);
            }
            for (var e : addE) {
                g.unmuteEdge(e);
            }

            Collections.shuffle(g.allEdges);
            start = System.currentTimeMillis();
            var k_tree = g.spanningTree_k();
            k_time += System.currentTimeMillis() - start;

            if (Math.abs(update_tree.cost - k_tree.cost) > 0.0001) {
                throw new Error();
            }
            prevTree = update_tree;
        }

        System.out.println("更新算法耗时：" + u_time);
        System.out.println("克鲁斯卡尔耗时：" + k_time);
    }
}
