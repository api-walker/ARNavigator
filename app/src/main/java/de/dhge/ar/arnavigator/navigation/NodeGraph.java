package de.dhge.ar.arnavigator.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class NodeGraph {
    private Map<String, Integer> nameToId;
    private Map<Integer, Node> idToNode;
    private ArrayList<String> names;

    public NodeGraph(String definition)
    {
        nameToId = new HashMap<>();
        idToNode = new HashMap<>();
        names = new ArrayList<>();

        parse(definition.replaceAll("\r", ""));
    }

    private void parse(String definition)
    {
        String[] lines = definition.split("\n");
        for (String line : lines) {
            String[] parts = line.split(" ");

            switch (parts[0]){
                case "#":
                    // Format: #_SPACE_Comment
                    // # Comment
                    // ex: # Exmaple comment
                    break;
                case "Node":
                case "N":
                    // Format: N|Node_SPACE_<Node's id>_SPACE_<Node's x>_SPACE_<Node's y>_SPACE_<Node's name without whitespace>
                    // N id x y name
                    // ex: Node 1 5 7 Hörsaal
                    int id = Integer.parseInt(parts[1]);
                    float x = Float.parseFloat(parts[2]);
                    float y = Float.parseFloat(parts[3]);
                    String name = parts[4];

                    // add name to list
                    names.add(name);

                    Node n = new Node(id, x, y, name);

                    nameToId.put(name, id);
                    idToNode.put(id, n);
                    break;
                case "Connection":
                case "Con":
                case "C":
                    // Format: C|Con|Connection_SPACE_<First Node's id>_SPACE_<Second Node's id>_SPACE_<distance between them as float>
                    // C id1 id2 dist
                    // ex: C 1 2 5.788
                    int firstId = Integer.parseInt(parts[1]);
                    int secondId = Integer.parseInt(parts[2]);
                    float dist = Float.parseFloat(parts[3]);

                    Node first = idToNode.get(firstId);
                    Node second = idToNode.get(secondId);

                    first.addConnection(secondId, dist);
                    second.addConnection(firstId, dist);
                    break;
            }
        }
    }

    public ArrayList<Node> getPath(String startName, String destinationName)
    {
        return getPath(nameToId.get(startName), nameToId.get(destinationName));
    }

    public ArrayList<Node> getPath(int startId, int destinationId)
    {
        ArrayList<Node> path = new ArrayList<>();

        ArrayList<Node> open = new ArrayList<>();
        ArrayList<Node> closed = new ArrayList<>();

        open.add(idToNode.get(startId));

        while (open.size() != 0)
        {
            Node current = open.get(0);
            if(current.getId() == destinationId) {
                reconstructPath(path, current);
                break;
            }

            open.remove(0);
            closed.add(current);

            int[] conns = current.getConnections();
            float[] dists = current.getDistances();
            for (int i = 0; i < conns.length; i++) {
                Node neighbour = idToNode.get(conns[i]);
                if(closed.contains(neighbour))
                    continue;

                float cost = current.cost + dists[i] + heuristic(current, neighbour);
                if(cost < neighbour.cost || !open.contains(neighbour))
                {
                    neighbour.cost = cost;
                    neighbour.parent = current;

                    if(!open.contains(neighbour))
                        open.add(neighbour);
                }
            }

            Collections.sort(open, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return Float.compare(o1.cost, o2.cost);
                }
            });
        }

        return path;
    }

    // Returns an array of names from the map
    public ArrayList<String> getNames() {
        return names;
    }

    private void reconstructPath(ArrayList<Node> path, Node target)
    {
        Node n = target;
        while (n != null)
        {
            path.add(n);
            n = n.parent;
        }
    }

    public float heuristic(Node a, Node b)
    {
        // Manhattan Distance
        float distX = Math.abs(b.x - a.x);
        float distY = Math.abs(b.y - a.y);
        return distX + distY;
    }
}
