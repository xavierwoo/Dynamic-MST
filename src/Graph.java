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
            var edge = new Edge(vertices.get(source), vertices.get(target), cost);
            allEdges.add(edge);
            edgeLists.get(source).add(edge);
            edgeLists.get(target).add(edge);
        }
        bf.close();
    }

    public void muteVertex(int id){
        vertices.get(id).isFeasible = false;
        vertices_count -= 1;
    }
    public void unmuteVertex(int id){
        vertices.get(id).isFeasible = true;
        vertices_count += 1;
    }

    public SpanningTree spanningTree_kruskal(){
        allEdges.sort(Comparator.comparingDouble(e -> e.cost));

        Arrays.fill(g_disjointSet, -1);

        var tree = new SpanningTree();
        tree.sortedAllEdges = new ArrayList<>(allEdges);
        for(var edge : allEdges){
            if(!edge.source.isFeasible || !edge.target.isFeasible)continue;
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
        //tree.disjointSet = g_disjointSet;
        return tree;
    }

    private void remove_v_from_tree(SpanningTree tree, Vertex removedV, List<Edge> remainingEdges, List<Vertex> conVertices){
        for(var e : tree.treeEdges){
            if(e.source == removedV){
                conVertices.add(e.target);
            }else if(e.target == removedV){
                conVertices.add(e.source);
            }else{
                remainingEdges.add(e);
            }
        }
    }

    private void rebuild_g_disjoint_set(List<Vertex> conVertices){

        Arrays.fill(g_disjointSet, -1);
        Arrays.fill(g_visited, false);

//        for(var v : conVertices){
//            g_visited[v.id] = true;
//            count_recur(v.id, v);
//        }
        int unvisited_id = find_unvisited();
        while(unvisited_id > -1){
            var v = vertices.get(unvisited_id);
            g_visited[unvisited_id] = true;
            count_recur(unvisited_id, v);
            unvisited_id = find_unvisited();
        }
    }

    private int find_unvisited(){
        for(int i=0; i<g_visited.length; ++i){
            if(!vertices.get(i).isFeasible)continue;
            if(!g_visited[i])return i;
        }
        return -1;
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

    public SpanningTree spanningTree_of_sub(SpanningTree oriTree, Vertex removedV){
        var remainingEdges = new ArrayList<Edge>();
        var conVertices = new ArrayList<Vertex>();
        remove_v_from_tree(oriTree, removedV, remainingEdges, conVertices);

        var tree = new SpanningTree();
        for(var e : remainingEdges){
            tree.addEdge(e);
        }

        if(conVertices.size() == 1){
            return tree;
        }

        for(var e : allEdges){
            e.isOn = false;
        }
        for(var e : remainingEdges){
            e.isOn = true;
        }

        rebuild_g_disjoint_set(conVertices);
        for(var edge : oriTree.sortedAllEdges){
            if(!edge.source.isFeasible || !edge.target.isFeasible || edge.isOn)continue;
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

    public SpanningTree spanningTree_of_super(SpanningTree oriTree, Vertex addedV){
        for(var e : allEdges){
            e.isOn = false;
        }
        var currEdges = new ArrayList<Edge>();
        for(var e : oriTree.treeEdges){
            e.isOn = true;
            currEdges.add(e);
        }
        for(var e : edgeLists.get(addedV.id)){
            e.isOn = true;
            currEdges.add(e);
        }
        currEdges.sort(Comparator.comparingDouble(e -> e.cost));
        int[] disjointSet = new int[vertices.size()];
        Arrays.fill(disjointSet, -1);
        var tree = new SpanningTree();
        for(var edge : currEdges){
            int i = edge.source.id;
            while(disjointSet[i] >= 0) i = disjointSet[i];
            int j = edge.target.id;
            while(disjointSet[j] >= 0) j = disjointSet[j];
            if(i != j){
                if(i < j){
                    disjointSet[i] += disjointSet[j];
                    disjointSet[j] = i;
                }else{
                    disjointSet[j] += disjointSet[i];
                    disjointSet[i] = j;
                }
                tree.addEdge(edge);
            }
            if(tree.treeEdges.size() >= vertices_count - 1)break;
        }
        return tree;
    }

    public SpanningTree spanningTree_update(SpanningTree oriTree, Vertex removeV, Vertex addV){
        muteVertex(addV.id);
        var rmv_spanningTree = spanningTree_of_sub(oriTree, removeV);
        //var tree_t = spanningTree_kruskal();
        unmuteVertex(addV.id);
        var tree =  spanningTree_of_super(rmv_spanningTree, addV, removeV);
        //check_tree(tree);
        return tree;
    }

    public SpanningTree spanningTree_of_super(SpanningTree oriTree, Vertex addedV, Vertex excludeV){
        for(var e : allEdges){
            e.isOn = false;
        }
        var currEdges = new ArrayList<Edge>();
        for(var e : oriTree.treeEdges){
            e.isOn = true;
            currEdges.add(e);
        }
        for(var e : edgeLists.get(addedV.id)){
            var v = e.getOtherEndV(addedV);
            if(v==excludeV)continue;
            e.isOn = true;
            currEdges.add(e);
        }
        currEdges.sort(Comparator.comparingDouble(e -> e.cost));
        int[] disjointSet = new int[vertices.size()];
        Arrays.fill(disjointSet, -1);
        var tree = new SpanningTree();
        for(var edge : currEdges){
            int i = edge.source.id;
            while(disjointSet[i] >= 0) i = disjointSet[i];
            int j = edge.target.id;
            while(disjointSet[j] >= 0) j = disjointSet[j];
            if(i != j){
                if(i < j){
                    disjointSet[i] += disjointSet[j];
                    disjointSet[j] = i;
                }else{
                    disjointSet[j] += disjointSet[i];
                    disjointSet[i] = j;
                }
                tree.addEdge(edge);
            }
            if(tree.treeEdges.size() >= vertices_count - 1)break;
        }
        return tree;
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
            String str = "";
            for(var e : treeEdges){
                str += e.source + "--" + e.target + "\n";
            }
            return str;
        }
    }

    class Vertex {
        int id;
        boolean isFeasible = true;
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