import com.google.gson.Gson;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.awt.Point;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


import static spark.Spark.*;

public class Main {

    private static final int ACTION_NONE = 0;
    private static final int ACTION_DROP_BOMB = 1;
    private static final int ACTION_FIRE_MISSILE = 2;

    private static final int MOVE_DIRECTION_NONE = -1;
    private static final int MOVE_DIRECTION_UP = 0;
    private static final int MOVE_DIRECTION_DOWN = 1;
    private static final int MOVE_DIRECTION_RIGHT = 2;
    private static final int MOVE_DIRECTION_LEFT = 3;

    private static final int TILE_NONE = 0;
    private static final int TILE_REGULAR = 1;
    private static final int TILE_FORTIFIED = 2;
    private static final int TILE_INDESTRUCTIBLE = 3;

    private static Gson gson = new Gson();
    private static int[][] tiles;
    private static Point botLocation = new Point();
    private static Point enemyLocation = new Point();
    private static int mapWidth;
    private static int mapHeight;
    private static org.slf4j.Logger logger;

    // decision configs
    private static final int maxEnemyDistance = 4;
    private static final int stuckDepth = 2;
    private static int currentRoundNumber;
    private static Map<Integer, Integer> moves = new HashMap<>();
    private static BattlefieldInfo battlefield;

    public static void main(String[] args) {

        // Requests

        post("/performnextmove", (req, res) -> prepareMoveResponse(req));

        get("/info", (req, res) -> " {" +
                "    \"Name\": \"908\"," +
                "    \"AvatarUrl\": \"https://trello-logos.s3.amazonaws.com/be589b2bdcbde18e4756e02d2f77017e/170.png\"," +
                "    \"Description\": \"There can be only one\"," +
                "    \"GameType\": \"TankBlaster\"" +
                "}");
    }


    // Logic

    private static String prepareMoveResponse(Request req) {
        setupLogger();
        parseCurrentState(req);
        BotMove move = calculateMove();
        return gson.toJson(move);
    }

    private static void setupLogger() {
        logger = LoggerFactory.getLogger("Main");
    }

    private static BotMove calculateMove() {
        BotMove move = new BotMove();
        move.Action = ACTION_NONE;
        move.FireDirection = MOVE_DIRECTION_NONE;
        move.Direction = MOVE_DIRECTION_NONE;
        int direction;
        List<Integer> directionsQue = new ArrayList<>();
        boolean isStepCloser = Math.abs(botLocation.x - enemyLocation.x) + Math.abs(botLocation.y - enemyLocation.y) > maxEnemyDistance;
        boolean dropBomb = false;
        boolean shotMissile = false;

        // calculate directions priority
        boolean isHorizontalDistanceBigger = (Math.abs(botLocation.x - enemyLocation.x) > Math.abs(botLocation.y - enemyLocation.y));
        if (isStepCloser) {
            if (botLocation.x > enemyLocation.x) {
                if (botLocation.y > enemyLocation.y) {
                    if (isHorizontalDistanceBigger) {
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                    } else {
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                    }
                } else {
                    if (isHorizontalDistanceBigger) {
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                    } else {
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_UP);
                    }
                }
            } else {
                if (botLocation.y > enemyLocation.y) {
                    if (isHorizontalDistanceBigger) {
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                    } else {
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                    }
                } else {
                    if (isHorizontalDistanceBigger) {
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                    } else {
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_UP);
                    }
                }
            }
        } else {
            if (botLocation.x > enemyLocation.x) {
                if (botLocation.y > enemyLocation.y) {
                    if (isHorizontalDistanceBigger) {
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_UP);
                    } else {
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                    }
                } else {
                    if (isHorizontalDistanceBigger) {
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                    } else {
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                    }
                }
            } else {
                if (botLocation.y > enemyLocation.y) {
                    if (isHorizontalDistanceBigger) {
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_UP);
                    } else {
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                    }
                } else {
                    if (isHorizontalDistanceBigger) {
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                    } else {
                        directionsQue.add(MOVE_DIRECTION_LEFT);
                        directionsQue.add(MOVE_DIRECTION_UP);
                        directionsQue.add(MOVE_DIRECTION_DOWN);
                        directionsQue.add(MOVE_DIRECTION_RIGHT);
                    }
                }
            }
            // don't drop many bombs to prevent suicide
            dropBomb = battlefield.Bombs.size()  <= 3;
        }
        for (Integer nextDirection : directionsQue) {
            if (isMoveValid(nextDirection) && isMoveSafe(nextDirection)) {
                move.Direction = nextDirection;
                break;
            }
        }
        moves.put(currentRoundNumber, move.Direction);
        if (isStuck()) {
            move.Direction = calculateAntistuckDirection();
        }
        if (dropBomb) {
            move.Action = ACTION_DROP_BOMB;
        } else {
            sendMissileIfNeed(move);
        }
        return move;
    }

    private static void sendMissileIfNeed(BotMove move) {
        // TODO add missile launch
    }

    private static int calculateAntistuckDirection() {
        for (int i = 0; i < 15; i++) {
            int randDirection = getRand(MOVE_DIRECTION_NONE, MOVE_DIRECTION_LEFT);
            if (isMoveValid(randDirection) && isMoveSafe(randDirection)) {
                moves.put(currentRoundNumber, randDirection);
                return randDirection;
            }
        }
        return suicideMove();

    }

    private static int suicideMove() {
        // TODO calculate suicide move fo draw
        return MOVE_DIRECTION_NONE;
    }

    private static boolean isStuck() {
        if (moves.size() < stuckDepth * 2) {
            return false;
        } else {
            for (int i = 0; i <= stuckDepth; i++) {
                int pos2 = i + 2;
                if (!Objects.equals(moves.get(currentRoundNumber - i), moves.get(currentRoundNumber - pos2))) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isMoveSafe(Integer nextDirection) {
        int botX = botLocation.x;
        int botY = botLocation.y;
        switch (nextDirection) {
            case MOVE_DIRECTION_UP:
                botY--;
                break;
            case MOVE_DIRECTION_DOWN:
                botY++;
                break;
            case MOVE_DIRECTION_LEFT:
                botX--;
                break;
            case MOVE_DIRECTION_RIGHT:
                botX++;
                break;
        }
        // check for move into bomb or missile explosion
        for (BattlefieldInfo.Bomb bomb : battlefield.Bombs) {
            String[] bombLocationParts = bomb.Location.split(",");
            Point bombLocation = new Point(Integer.parseInt(bombLocationParts[0].trim()), Integer.parseInt(bombLocationParts[1].trim()));
            if (bombLocation.x == botX && bombLocation.y == botY) {
                return false;
            }
            List<Point> explodePoints = new ArrayList<>();
            for (int i = 1; i <= bomb.ExplosionRadius; i++) {
                explodePoints.add(new Point(bombLocation.x + i, bombLocation.y));
                explodePoints.add(new Point(bombLocation.x, bombLocation.y + i));
                explodePoints.add(new Point(bombLocation.x - i, bombLocation.y));
                explodePoints.add(new Point(bombLocation.x, bombLocation.y - i));
            }
            for (Point explodePoint : explodePoints) {
                if (explodePoint.x == botX && explodePoint.y == botY) {
                    return false;
                }
            }
        }
        for (BattlefieldInfo.Missile missile : battlefield.Missiles) {
            String[] missileLocationParts = missile.Location.split(",");
            Point missileLocation = new Point(Integer.parseInt(missileLocationParts[0].trim()), Integer.parseInt(missileLocationParts[1].trim()));
            if (missileLocation.x == botX && missileLocation.y == botY) {
                return false;
            }
            // calculate missile explosion variants
            int movesToRun = battlefield.GameConfig.IsFastMissileModeEnabled ? 2 : 1;
            Point pointToCheck = new Point(missileLocation.x, missileLocation.y);
            switch (missile.MoveDirection) {
                case MOVE_DIRECTION_UP:
                    pointToCheck.y = pointToCheck.y - movesToRun;
                    missileLocation.y = missileLocation.y - (movesToRun - 1);
                    break;
                case MOVE_DIRECTION_DOWN:
                    pointToCheck.y = pointToCheck.y + movesToRun;
                    missileLocation.y = missileLocation.y + (movesToRun - 1);
                    break;
                case MOVE_DIRECTION_LEFT:
                    pointToCheck.x = pointToCheck.x - movesToRun;
                    missileLocation.x = missileLocation.x - (movesToRun - 1);
                    break;
                case MOVE_DIRECTION_RIGHT:
                    pointToCheck.x = pointToCheck.x + movesToRun;
                    missileLocation.x = missileLocation.x + (movesToRun - 1);
                    break;
                default:
                    // nothing to do
            }
            if (tiles[pointToCheck.x][pointToCheck.y] != TILE_NONE) {
                List<Point> explodePoints = new ArrayList<>();
                for (int i = 1; i <= missile.ExplosionRadius; i++) {
                    explodePoints.add(new Point(missileLocation.x + i, missileLocation.y));
                    explodePoints.add(new Point(missileLocation.x, missileLocation.y + i));
                    explodePoints.add(new Point(missileLocation.x - i, missileLocation.y));
                    explodePoints.add(new Point(missileLocation.x, missileLocation.y - i));
                }
                for (Point explodePoint : explodePoints) {
                    if (explodePoint.x == botX && explodePoint.y == botY) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean isMoveValid(int randMove) {
        Point newBotLocation = new Point(botLocation.x, botLocation.y);
        switch (randMove) {
            case MOVE_DIRECTION_UP:
                newBotLocation.y--;
                break;
            case MOVE_DIRECTION_DOWN:
                newBotLocation.y++;
                break;
            case MOVE_DIRECTION_LEFT:
                newBotLocation.x--;
                break;
            case MOVE_DIRECTION_RIGHT:
                newBotLocation.x++;
                break;
            default:
                // nothing to do
        }

        boolean isValid = newBotLocation.x < mapWidth && newBotLocation.y < mapHeight
                && newBotLocation.x > 0 && newBotLocation.y > 0
                && tiles[newBotLocation.x][newBotLocation.y] == TILE_NONE
                && (newBotLocation.x != enemyLocation.x && newBotLocation.y != enemyLocation.y);
        logger.error("Move direction: " + randMove + " | isValid: " + isValid);
        return isValid;
    }

    private static void parseCurrentState(Request req) {
        battlefield = gson.fromJson(req.body(), BattlefieldInfo.class);
        mapWidth = battlefield.GameConfig.MapWidth;
        mapHeight = battlefield.GameConfig.MapHeight;
        tiles = new int[mapWidth][mapHeight];
        battlefield.Board.forEach(item -> tiles[item.get(0)][item.get(1)] = item.get(2));
        String[] botLocationData = battlefield.BotLocation.split(",");
        botLocation.x = Integer.parseInt(botLocationData[0].trim());
        botLocation.y = Integer.parseInt(botLocationData[1].trim());
        String[] enemyLocationString = battlefield.OpponentLocations.get(0).split(",");
        enemyLocation.x = Integer.parseInt(enemyLocationString[0].trim());
        enemyLocation.y = Integer.parseInt(enemyLocationString[1].trim());
        currentRoundNumber = battlefield.RoundNumber;
    }

    private static int getRand(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}