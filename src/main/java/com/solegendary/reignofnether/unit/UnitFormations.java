package com.solegendary.reignofnether.unit;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UnitFormations {

    // given a list of entities that were issued a move command to moveBp, find the best pairings of entities and
    // positions that allows each entity the best chance to reach said position without bumping into each other

    public static List<Pair<Integer, BlockPos>> getMoveFormation(Level level, List<LivingEntity> units, BlockPos moveBp) {

        // 1. create a list of final BPs the units should move into, closest approximation of a square
        //    with moveBp at the approximate centre by spiralling outward
        List<BlockPos> bps = new ArrayList<>(List.of(moveBp));
        BlockPos lastBp = bps.get(0);

        if (units.size() == 3) {
            // for specifically size 3 just use a 3v1 line
            bps.add(bps.get(0).west());
            bps.add(bps.get(0).east());
        } else {
            Direction dir = Direction.NORTH;
            double edgeLength = 1.1f;
            int edgeBlocksLeft = 0;
            for (int i = 0; i < units.size() - 1; i++) {
                BlockPos nextBp = switch (dir) {
                    case NORTH -> lastBp.north();
                    case SOUTH -> lastBp.south();
                    case WEST -> lastBp.west();
                    case EAST -> lastBp.east();
                    default -> null;
                };
                bps.add(nextBp);
                lastBp = nextBp;
                edgeBlocksLeft -= 1;
                if (edgeBlocksLeft <= 0) {
                    edgeLength += 0.5;
                    edgeBlocksLeft = (int) Math.floor(edgeLength);
                    dir = switch (dir) {
                        case NORTH -> Direction.WEST;
                        case WEST -> Direction.SOUTH;
                        case SOUTH -> Direction.EAST;
                        case EAST -> Direction.NORTH;
                        default -> null;
                    };
                }
            }
        }

        // 2. pair each unit to each square based on min/max x/z positions
        //    eg. the north-west-most unit should go to the north-west-most square
        units.sort(Comparator.comparing(u -> u.getOnPos().getX()));
        units.sort(Comparator.comparing(u -> u.getOnPos().getZ()));
        bps.sort(Comparator.comparing(Vec3i::getX));
        bps.sort(Comparator.comparing(Vec3i::getZ));

        // entityId/BlockPos pairs
        List<Pair<Integer, BlockPos>> formation = new ArrayList<>();

        for (int i = 0; i < units.size(); i++) {
            formation.add(new Pair<>(units.get(i).getId(), bps.get(i)));
        }
        return formation;
    }

}
