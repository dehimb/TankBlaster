import java.util.List;

/**
 * Created by dehimb on 12/14/16.
 */
public class BattlefieldInfo {
    GameConfig GameConfig;

    int RoundNumber;
    String BotId;
    List<List<Integer>> Board;
    String BotLocation;
    int MissileAvailableIn;
    List<String> OpponentLocations;
    List<Bomb> Bombs;
    List<Missile> Missiles;

    static class Bomb {
        public Bomb(int roundsUntilExplodes, String location, int explosionRadius) {
            RoundsUntilExplodes = roundsUntilExplodes;
            Location = location;
            ExplosionRadius = explosionRadius;
        }

        int RoundsUntilExplodes;
        String Location;
        int ExplosionRadius;

    }

    static class Missile {
        public Missile(int moveDirection, String location, int explosionRadius) {
            MoveDirection = moveDirection;
            Location = location;
            ExplosionRadius = explosionRadius;
        }

        int MoveDirection;
        String Location;
        int ExplosionRadius;

    }

    static class GameConfig {
        public GameConfig(int mapWidth, int mapHeight, int bombBlastRadius, int missileBlastRadius, int roundsBetweenMissiles, int roundsBeforeIncreasingBlastRadius, boolean isFastMissileModeEnabled) {
            MapWidth = mapWidth;
            MapHeight = mapHeight;
            BombBlastRadius = bombBlastRadius;
            MissileBlastRadius = missileBlastRadius;
            RoundsBetweenMissiles = roundsBetweenMissiles;
            RoundsBeforeIncreasingBlastRadius = roundsBeforeIncreasingBlastRadius;
            IsFastMissileModeEnabled = isFastMissileModeEnabled;
        }

        int MapWidth;
        int MapHeight;
        int BombBlastRadius;
        int MissileBlastRadius;
        int RoundsBetweenMissiles;
        int RoundsBeforeIncreasingBlastRadius;
        boolean IsFastMissileModeEnabled;
    }
}