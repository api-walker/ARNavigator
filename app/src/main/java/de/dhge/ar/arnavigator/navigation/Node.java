package de.dhge.ar.arnavigator.navigation;

import java.util.ArrayList;

public class Node {
    private ArrayList<Integer> mConnections;
    private ArrayList<Float> mDistances;

    private int mId;
    private String mName;

    public final float x, y;
    public Node parent;
    public float cost;

    public Node(int id, float x, float y, String name)
    {
        this.x = x;
        this.y = y;

        mId = id;
        mName = name;

        mConnections = new ArrayList<>();
        mDistances = new ArrayList<>();
    }

    public void addConnection(int targetNode, float distance)
    {
        if(targetNode == mId)
            throw new IllegalArgumentException("\"targetNode\" may not be the same node!");
        if(mConnections.contains(targetNode))
            throw new IllegalArgumentException("\"targetNode\" already contained in connections!");
        if(distance <= 0)
            throw new IllegalArgumentException("\"distance\" has to be greater than 0!");

        mConnections.add(targetNode);
        mDistances.add(distance);
    }

    public int getId()
    {
        return mId;
    }

    public int getConnectionCount()
    {
        return mConnections.size();
    }

    public int[] getConnections()
    {
        int[] result = new int[mConnections.size()];

        for (int i = 0; i < result.length; i++)
            result[i] = mConnections.get(i).intValue();

        return result;
    }

    public float[] getDistances()
    {
        float[] result = new float[mDistances.size()];

        for (int i = 0; i < result.length; i++)
            result[i] = mDistances.get(i).floatValue();

        return result;
    }

    @Override
    public String toString()
    {
        return mId + ": " + mName;
    }
}
