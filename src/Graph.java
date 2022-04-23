import java.io.*;
import java.util.*;

public class Graph {
    int vertices_count;
    List<Vertex> vertices;
    List<List<Edge> > edgeLists;
    List<Edge> allEdges;

    int[] g_disjointSet;
    boolean[] g_visited;

    Graph(String instance_file) throws IOException {
        var bf = new BufferedReader(new FileReader(instance_file));
        String line = bf.readLine();
        String[] data = line.split(" ");
        int numV = Integer.parseInt(data[0]);
        int numE = Integer.parseInt(data[1]);
        vertices = new ArrayList<>(numV);
        vertices_count = numV;
        edgeLists = new ArrayList<>(numV);
        allEdges = new ArrayList<>(numE);

        g_disjointSet = new int[numV];
        g_visited = new boolean[numV];

        for(int i=0; i<numV; ++i){
            vertices.add(new Vertex(i));
            edgeLists.add(new ArrayList<>());
        }

        vertices = Collections.unmodifiableList(vertices);

        for(int i=0; i<numE; ++i){
            line = bf.readLine();
            data = line.split(" ");
            int source = Integer.parseInt(data[0]);
            int target = Integer.parseInt(data[1]);
            double cost = Double.parseDouble(data[2]);
            Vertex s = vertices.get(source);
            Vertex t = vertices.get(target);
            var edge = new Edge(s, t, cost);
            allEdges.add(edge);
            s.degree += 1;
            s.isMute = false;
            t.degree += 1;
            t.isMute = false;
            edgeLists.get(source).add(edge);
            edgeLists.get(target).add(edge);

        }
        bf.close();
    }

    public List<Edge> allUnmuteEdges(){
        var unmute_edges = new ArrayList<Edge>();
        for(var e: allEdges){
            if(!e.isMute){
                unmute_edges.add(e);
            }
        }
        return unmute_edges;
    }

    public SpanningTree spanningTree_k(){
        allEdges.sort(Comparator.comparingDouble(e -> e.cost));

        Arrays.fill(g_disjointSet, -1);

        var tree = new SpanningTree();
        tree.sortedAllEdges = new ArrayList<>(allEdges);
        for(var edge : allEdges){
            if(edge.isMute)continue;
            int i = edge.source.id;
            while(g_disjointSet[i] >= 0) i = g_disjointSet[i];
            int j = edge.target.id;
            while(g_disjointSet[j] >= 0) j = g_disjointSet[j];
            if(i != j){
                if(i < j){
                    g_disjointSet[i] += g_disjointSet[j];
                    g_disjointSet[j] = i;
                }else{
                    g_disjointSet[j] += g_disjointSet[i];
                    g_disjointSet[i] = j;
                }
                tree.addEdge(edge);
            }
            if(tree.treeEdges.size() >= vertices_count - 1)break;
        }
        return tree;
    }

    private void count_recur(int source_id, Vertex curr){
        var el = edgeLists.get(curr.id);
        for(var e : el){
            if(e.isOn){
                Vertex u = e.getOtherEndV(curr);
                if(!g_visited[u.id]){
                    g_visited[u.id] = true;
                    g_disjointSet[source_id] -= 1;
                    g_disjointSet[u.id] = source_id;
                    count_recur(source_id, u);
                }
            }
        }
    }

    private void remove_e_from_tree(SpanningTree tree, List<Edge> removedE, List<Edge> remainingEdges){
        for(var e : removedE){
            muteEdge(e);
        }

        for(var e: tree.treeEdges){
            if(!e.isMute){
                remainingEdges.add(e);
            }
        }
    }


    private int find_unMute_unvisited(){
        for(int i=0; i<g_visited.length; ++i){
            if(vertices.get(i).isMute)continue;
            if(!g_visited[i])return i;
        }
        return -1;
    }

    //return the sub-components number
    private int rebuild_disjoint_set(){
        Arrays.fill(g_disjointSet, -1);
        Arrays.fill(g_visited, false);

        int unvisited_id = find_unMute_unvisited();
        int components_count = 0;
        while(unvisited_id > -1){
            components_count++;
            Vertex v = vertices.get(unvisited_id);
            g_visited[unvisited_id] = true;
            count_recur(unvisited_id, v);
            unvisited_id = find_unMute_unvisited();
        }
        return components_count;
    }

    public SpanningTree spanningTree_of_sub(SpanningTree oriTree, List<Edge> removedE){
        var remainingEdges = new ArrayList<Edge>();
        remove_e_from_tree(oriTree, removedE, remainingEdges);

        var tree = new SpanningTree();
        tree.sortedAllEdges = oriTree.sortedAllEdges;
        for(var e : remainingEdges){
            tree.addEdge(e);
        }

        for(var e : allEdges){
            e.isOn = false;
        }
        for(var e : remainingEdges){
            e.isOn = true;
        }
        int components_count = rebuild_disjoint_set();
        if(components_count == 1)return tree;
        
        for(var edge : oriTree.sortedAllEdges){
            if(edge.isMute || edge.isOn)continue;
            int i = edge.source.id;
            while(g_disjointSet[i] >= 0) i = g_disjointSet[i];
            int j = edge.target.id;
            while(g_disjointSet[j] >= 0) j = g_disjointSet[j];
            if(i != j){
                if(i < j){
                    g_disjointSet[i] += g_disjointSet[j];
                    g_disjointSet[j] = i;
                }else{
                    g_disjointSet[j] += g_disjointSet[i];
                    g_disjointSet[i] = j;
                }
                tree.addEdge(edge);
            }
            if(tree.treeEdges.size() >= vertices_count - 1)break;
        }
        return tree;
    }

    public SpanningTree spanningTree_of_super(SpanningTree oriTree, List<Edge> addE){

        for(var e : allEdges){
            e.isOn = false;
        }

        var currEdges = new ArrayList<Edge>();
        for(var e : addE){
            unmuteEdge(e);
            e.isOn = true;
            currEdges.add(e);
        }

        for(var e : oriTree.treeEdges){
            e.isOn = true;
           currEdges.add(e);
        }
        currEdges.sort(Comparator.comparingDouble(e -> e.cost));
        Arrays.fill(g_disjointSet, -1);
        var tree = new SpanningTree();
        for(var edge : currEdges){
            if(edge.isMute)continue;
            int i = edge.source.id;
            while(g_disjointSet[i] >= 0) i = g_disjointSet[i];
            int j = edge.target.id;
            while(g_disjointSet[j] >= 0) j = g_disjointSet[j];
            if(i != j){
                if(i < j){
                    g_disjointSet[i] += g_disjointSet[j];
                    g_disjointSet[j] = i;
                }else{
                    g_disjointSet[j] += g_disjointSet[i];
                    g_disjointSet[i] = j;
                }
                tree.addEdge(edge);
            }
            if(tree.treeEdges.size() >= vertices_count - 1)break;
        }
        tree.sortedAllEdges = sim_gen_new_sorted_edges(oriTree.sortedAllEdges, addE);
        return tree;
    }

    private List<Edge> sim_gen_new_sorted_edges(List<Edge> ori, List<Edge> addE){
        addE.sort(Comparator.comparingDouble(e -> e.cost));
        var new_list = new ArrayList<Edge>(ori.size());
        int i=0, j=0;
        while(i<ori.size() && j<addE.size()){
            while(ori.get(i).isMute)i++;
            if(i>=ori.size())break;
            if(ori.get(i).cost < addE.get(j).cost){
                new_list.add(ori.get(i));
                i++;
            }else{
                new_list.add(addE.get(j));
                j++;
            }
        }
        if(i== ori.size()){
            for(int k =j; k < addE.size(); ++k){
                new_list.add(addE.get(k));
            }
        }else{
            for(int k=i; k< ori.size(); ++k){
                new_list.add(ori.get(k));
            }
        }
        return new_list;
    }

    void muteEdge(Edge e){
        if(e.isMute)return;
        e.isMute = true;
        e.source.degree--;
        if(e.source.degree == 0){
            e.source.isMute = true;
            vertices_count--;
        }
        e.target.degree--;
        if(e.target.degree == 0){
            e.target.isMute = true;
            vertices_count--;
        }
    }

    void unmuteEdge(Edge e){
        if(!e.isMute)return;
        e.isMute = false;
        e.source.degree++;
        if(e.source.degree == 1){
            e.source.isMute = false;
            vertices_count++;
        }
        e.target.degree++;
        if(e.target.degree == 1){
            e.target.isMute = false;
            vertices_count++;
        }
    }

    class SpanningTree {
        List<Edge> treeEdges;
        double cost;
        List<Edge> sortedAllEdges;
        SpanningTree(){
            treeEdges = new ArrayList<>();
            cost = 0;
        }
        void addEdge(Edge e){
            treeEdges.add(e);
            cost += e.cost;
        }

        public String toString(){
            StringBuilder str = new StringBuilder();
            for(var e : treeEdges){
                str.append(e.source).append("--").append(e.target).append("\n");
            }
            return str.toString();
        }
    }

    class Vertex {
        int id;
        int degree = 0;
        boolean isMute = true;
        Vertex(int id){
            this.id = id;
        }
        public String toString(){
            return Integer.toString(id);
        }
    }

    class Edge {
        Vertex source;
        Vertex target;
        double cost;
        boolean isOn = true;
        boolean isMute = false;

        Edge(Vertex s, Vertex t, double c){
            source = s;
            target = t;
            cost = c;
        }

        public String toString(){
            return "(" + source.id + "," + target.id + ")" + cost;
        }

        Vertex getOtherEndV(Vertex v){
            if(source == v){
                return target;
            }else{
                return source;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return source.equals(edge.source) && target.equals(edge.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target);
        }
    }
}
