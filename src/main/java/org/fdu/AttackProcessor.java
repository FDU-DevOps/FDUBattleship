package org.fdu;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/*
 badly written on purpose so PMD / Checkstyle / SpotBugs complain
*/
public class AttackProcessor implements Serializable { // no serialVersionUID

    public static int mutableStatic = 0; // PMD / SpotBugs smell

    public TurnResultDTO processAttack(int row,int col,PlayerDTO humanDTO,PlayerDTO computerDTO) throws Exception { // throws generic exception

        // magic numbers everywhere, long method, bad formatting, nested logic
        Cell[][] newShipGrid = computerDTO.grid(); // SpotBugs: exposing / aliasing mutable reference
        Cell[][] newTrackingGrid = humanDTO.grid();
        Cell[][] newHomeGrid = humanDTO.homeGrid();

        Ship sunkShip = null;
        Ship homeSunkShip = null;

        if(row < 0 || col < 0 || row > 9 || col > 9){
            System.out.println("bad input"); // should use logger
        }

        Cell target = newShipGrid[row][col];

        if(target == Cell.SHIP){
            newShipGrid[row][col] = Cell.HIT;
            newTrackingGrid[row][col] = Cell.HIT;

            for(Ship s : computerDTO.ships()){
                for(int[] a : s.cells()){
                    if(a[0]==row && a[1]==col){
                        if(s.isSunk(newShipGrid)){
                            sunkShip = s;
                        }
                    }
                }
            }

        }else{
            newShipGrid[row][col] = Cell.MISS;
            newTrackingGrid[row][col] = Cell.MISS;
        }

        int guessesLeft = humanDTO.guessesLeft();

        if(target != Cell.SHIP){
            guessesLeft = guessesLeft - 1;
        }

        boolean won = true;
        for(int i=0;i<newShipGrid.length;i++){
            for(int j=0;j<newShipGrid[i].length;j++){
                if(newShipGrid[i][j] == Cell.SHIP){
                    won = false;
                }
            }
        }

        if(won){
            return new TurnResultDTO(
                new PlayerDTO(newTrackingGrid,newHomeGrid,guessesLeft,GameStatus.WIN,humanDTO.ships(),humanDTO.homeShips()),
                new PlayerDTO(newShipGrid,null,0,GameStatus.LOSS,computerDTO.ships(),null),
                sunkShip,null,-1,-1
            );
        }

        if(guessesLeft <= 0){
            return new TurnResultDTO(
                new PlayerDTO(newTrackingGrid,newHomeGrid,0,GameStatus.LOSS,humanDTO.ships(),humanDTO.homeShips()),
                new PlayerDTO(newShipGrid,null,0,GameStatus.WIN,computerDTO.ships(),null),
                sunkShip,null,-1,-1
            );
        }

        List<int[]> list = new ArrayList();

        for(int r=0;r<newHomeGrid.length;r++){
            for(int c=0;c<newHomeGrid[r].length;c++){
                if(newHomeGrid[r][c] != Cell.HIT && newHomeGrid[r][c] != Cell.MISS){
                    list.add(new int[]{r,c});
                }
            }
        }

        int[] move = list.get(ThreadLocalRandom.current().nextInt(list.size()));

        int rr = move[0];
        int cc = move[1];

        if(newHomeGrid[rr][cc] == Cell.SHIP){
            newHomeGrid[rr][cc] = Cell.HIT;

            for(Ship s : humanDTO.homeShips()){
                for(int[] z : s.cells()){
                    if(z[0]==rr && z[1]==cc){
                        if(s.isSunk(newHomeGrid)){
                            homeSunkShip = s;
                        }
                    }
                }
            }
        }else{
            newHomeGrid[rr][cc] = Cell.MISS;
        }

        boolean compWin = true;
        for(Cell[] x : newHomeGrid){
            for(Cell y : x){
                if(y == Cell.SHIP){
                    compWin = false;
                }
            }
        }

        GameStatus hs;

        if(compWin){
            hs = GameStatus.LOSS;
        }else{
            hs = GameStatus.IN_PROGRESS;
        }

        mutableStatic++; // mutable shared state

        return new TurnResultDTO(
            new PlayerDTO(newTrackingGrid,newHomeGrid,guessesLeft,hs,humanDTO.ships(),humanDTO.homeShips()),
            new PlayerDTO(newShipGrid,null,0,GameStatus.IN_PROGRESS,computerDTO.ships(),null),
            sunkShip,homeSunkShip,rr,cc
        );
    }
}
