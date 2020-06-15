/*
* MegaMek -
* Copyright (C) 2020 The MegaMek Team
*
* This program is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free Software
* Foundation; either version 2 of the License, or (at your option) any later
* version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
* details.
*/

package megamek.common.pathfinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import megamek.common.BulldozerMovePath;
import megamek.common.Coords;
import megamek.common.IHex;
import megamek.common.MovePath;
import megamek.common.MovePath.MoveStepType;

/**
 * This class contains functionality that takes a given path
 * and generates a list of child paths that go up to walk/run/run+masc/sprint/sprint+masc MP usage.
 * @author NickAragua
 */
public class PathDecorator {
    
    /**
     * Takes the given path and returns a list of child paths that go up to walk/run/run+masc/sprint/sprint+masc MP usage.
     */
    public static Set<MovePath> decoratePath(BulldozerMovePath source) {
        Set<MovePath> retVal = new HashSet<>();
     
        // we want to generate the following paths and decorations:
        // a "walking" path
        // a "running" path
        // a "running masc" path
        // a "sprinting" path
        // a "sprint with masc" path
        // decorations are movement possibilities that "fill up" any remaining MP with turns and unrelated moves
        
        MovePath clippedSource = source.clone();
        clippedSource.clipToPossible();
        
        Set<Integer> desiredMPs = new HashSet<>();
        desiredMPs.add(source.getCachedEntityState().getSprintMP());
        desiredMPs.add(source.getCachedEntityState().getSprintMPwithoutMASC());
        desiredMPs.add(source.getCachedEntityState().getRunMP());
        desiredMPs.add(source.getCachedEntityState().getRunMPwithoutMASC());
        desiredMPs.add(source.getCachedEntityState().getWalkMP());
        
        for(int desiredMP : desiredMPs) {
            List<MovePath> clippedPaths = clipToDesiredMP(clippedSource, desiredMP);
            retVal.addAll(clippedPaths);
        }
        
        return retVal;
    }
    
    /**
     * Clips the given path until it only uses the desired MP or less.
     */
    public static List<MovePath> clipToDesiredMP(MovePath source, int desiredMP) {
        MovePath newPath = source.clone();
        while(newPath.getMpUsed() > desiredMP) {
            newPath.removeLastStep();
        }
        
        List<MovePath> clippedPaths = generatePossiblePaths(newPath, desiredMP);
        
        return clippedPaths;
    }
    
    /**
     * Uses the LongestPathFinder to generate all paths possible from a starting path,
     * up to the desired MP
     */
    public static List<MovePath> generatePossiblePaths(MovePath source, int desiredMP) {
        List<MovePath> turnPaths = new ArrayList<>();
        
        LongestPathFinder lpf = LongestPathFinder
                .newInstanceOfLongestPath(desiredMP,
                        MoveStepType.FORWARDS, source.getGame());
        lpf.run(source);
        turnPaths.addAll(lpf.getLongestComputedPaths());
        
        return turnPaths;
    }
    
    /**
     * Given a path, adds "UP" steps to it so that a forward movement can pass over intervening terrain
     */
    public static void AdjustElevationForForwardMovement(MovePath source) {
        // get the hex that is in the direction we're facing
        Coords destinationCoords = source.getFinalCoords().translated(source.getFinalFacing());
        IHex destHex = source.getGame().getBoard().getHex(destinationCoords);
        
        if(destHex == null) {
            return;
        }
        
        // determine if the destination hex allows the unit in question to go up
        // if not, we're done here
        int entityElevation = source.getFinalElevation();
        boolean canGoUp = source.getEntity().canGoUp(entityElevation, source.getFinalCoords());
        if(!canGoUp) {
            return;
        }
        
        // add as many UP steps as MP will allow, until we are above whatever terrain feature is in the way. 
        int desiredElevation = destHex.maxTerrainFeatureElevation(false);
        
        while(entityElevation <= desiredElevation) {
            source.addStep(MoveStepType.UP);
            
            if(!source.isMoveLegal()) {
                source.removeLastStep();
                return;
            }
            
            entityElevation++;
        }
    }
}