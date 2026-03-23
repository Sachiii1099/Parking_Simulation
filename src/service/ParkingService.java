package service;
import java.util.*;
import model.Parking;
import model.Cell;
import model.enums.CellType;




public class ParkingService {
    public List<Cell> findPath(Parking lot,Cell start ,Cell end){
        boolean [][]visited =new boolean[lot.getRows()][lot.getCols()];
        Map<Cell,Cell> parent = new HashMap<>();
        Queue<Cell> queue = new LinkedList<>();
        queue.add(start);
        visited[start.getRow ()][start.getCol()]=true;
        int[] rowDirection={-1,1,0,0};
        int[] colDirection={0,0,-1,1};
        while(!queue.isEmpty()){
            Cell curr= queue.poll();
            if(curr==end){
                //path recontruction;
                return pathReconstruction(parent,start,end);
            }
            for(int i=0;i<4;i++){
                int newRow=curr.getRow()+rowDirection[i];
                int newCol= curr.getCol()+colDirection[i];

                if(newRow>=0&&newRow<lot.getRows()&&newCol>=0&&newCol<lot.getCols()&&!visited[newRow][newCol]&&lot.getGrid()[newRow][newCol].getType()!=CellType.BLOCK && !(lot.getGrid()[newRow][newCol].isOccupied()
                        && lot.getGrid()[newRow][newCol] != end)){

                    visited[newRow][newCol]=true;
                    parent.put(lot.getGrid()[newRow][newCol],curr);
                    queue.add(lot.getGrid()[newRow][newCol]);
                }
            }


        }
        //here we changed the return null to return an empty array list
        return new ArrayList<>();
    }


    private List<Cell> pathReconstruction(Map<Cell,Cell>parent,Cell start,Cell end){
        List<Cell> path = new ArrayList<>();
        Cell  curr=end;
        while(curr!=null){
            path.add(curr);
            curr=parent.get(curr);
        }
        Collections.reverse(path);
        return path;
    }
}
