public class StudentPlayer extends Player {
    public StudentPlayer(int playerIndex, int[] boardSize, int nToConnect) {
        super(playerIndex, boardSize, nToConnect);
    }

    public int max_depth = 7;
    public int initial_depth = 3;

    public int aiPlayer = 1; // The AI player's index
    public int opponent = 2; // The opponent's index
    public int nMoves = 0; // The number of moves made in the game

    public int alpha = Integer.MIN_VALUE;
    public int beta = Integer.MAX_VALUE;

    public int centerFactor = 3;

    public int evaluate(Board position) {

        int score = 0;
        int rows = 6;
        int cols = 7;

        // Evaluate the rows
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols - 3; col++) {
                int[] rowTokens = new int[4];
                rowTokens[0] = position.getState()[row][col];
                rowTokens[1] = position.getState()[row][col + 1];
                rowTokens[2] = position.getState()[row][col + 2];
                rowTokens[3] = position.getState()[row][col + 3];
                score += evaluateSequence(rowTokens);
            }
        }

        // Evaluate the columns
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows - 3; row++) {
                int[] colTokens = new int[4];
                colTokens[0] = position.getState()[row][col];
                colTokens[1] = position.getState()[row + 1][col];
                colTokens[2] = position.getState()[row + 2][col];
                colTokens[3] = position.getState()[row + 3][col];
                score += evaluateSequence(colTokens);
            }
        }

        // Evaluate Diagonals (bottom left to top right)
        for (int row = 3; row < rows; row++) {
            for (int col = 0; col < cols - 3; col++) {
                int[] diagonalTokens = new int[4];
                diagonalTokens[0] = position.getState()[row][col];
                diagonalTokens[1] = position.getState()[row - 1][col + 1];
                diagonalTokens[2] = position.getState()[row - 2][col + 2];
                diagonalTokens[3] = position.getState()[row - 3][col + 3];
                score += evaluateSequence(diagonalTokens);
            }
        }

        // Evaluate Diagonals (top left to bottom right)
        for (int row = 0; row < rows - 3; row++) {
            for (int col = 0; col < cols - 3; col++) {
                int[] diagonalTokens = new int[4];
                diagonalTokens[0] = position.getState()[row][col];
                diagonalTokens[1] = position.getState()[row + 1][col + 1];
                diagonalTokens[2] = position.getState()[row + 2][col + 2];
                diagonalTokens[3] = position.getState()[row + 3][col + 3];
                score += evaluateSequence(diagonalTokens);
            }
        }

        // Add a center preference heuristic
        int centerCol = cols / 2;
        for (int col = 0; col < cols; col++) {
            score += (centerCol - Math.abs(col - centerCol)) * centerFactor;
        }

        return score;
    }

    public int evaluateSequence(int[] sequence) {
        int score = 0;
        int aiPlayerCount = 0;
        int opponentCount = 0;
        int emptyCount = 0;

        for (int i = 0; i < sequence.length; i++) {
            if (sequence[i] == aiPlayer) {
                aiPlayerCount++;
            } else if (sequence[i] == opponent) {
                opponentCount++;
            } else {
                emptyCount++;
            }
        }

        // Assign scores based on the counts in the sequence
        if (aiPlayerCount == 4) {
            // AI player has four in a row - highly favorable
            score += 50000;
        } else if (opponentCount == 4) {
            // Opponent has four in a row - very unfavorable
            score -= 80000;// 100000
        } else if (aiPlayerCount == 3 && emptyCount == 1) {
            // AI player has three in a row with an open end
            score += 5000;
        } else if (opponentCount == 3 && emptyCount == 1) {
            // Opponent has three in a row with an open end
            score -= 8000; // 10000
        }
        // Check for sequences with potential
        else if (aiPlayerCount == 2 && emptyCount == 2) {
            // Two AI player tokens with two empty spaces - potential to form 4 in a row
            score += 500;
        } else if (opponentCount == 2 && emptyCount == 2) {
            // Two AI player tokens with two empty spaces - potential to form 4 in a row
            score -= 800; // 1000
        }

        return score;
    }

    public int minimax(Board position, int depth, boolean maximizingPlayer) {
        // Check if we've reached the 0 depth or if the game has ended.
        if (depth == 0 || position.gameEnded()) {
            return evaluate(position);
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < position.getValidSteps().size(); i++) {
                Board copy = new Board(position);
                copy.step(aiPlayer, i);
                int eval = minimax(copy, depth - 1, false);
                maxEval = Math.max(maxEval, eval);
                if (maxEval > alpha) {
                    alpha = maxEval;
                }
                if (beta <= alpha) {
                    break;
                }
            }

            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < position.getValidSteps().size(); i++) {
                Board copy = new Board(position);
                copy.step(opponent, i);
                int eval = minimax(copy, depth - 1, true);
                minEval = Math.min(minEval, eval);
                if (beta < minEval) {
                    beta = minEval;
                }
                if (beta <= alpha) {
                    break;
                }
            }

            return minEval;
        }
    }

    @Override
    public int step(Board board) {
        int bestScore = Integer.MIN_VALUE;
        int bestCol = 0;
        int currentDepth = 1;

        // determine the number of moves made
        for (int row = 0; row < board.getState().length; row++) {
            for (int col = 0; col < board.getState()[0].length; col++) {
                if (board.getState()[row][col] != 0) {
                    nMoves++;
                }
            }
        }

        // if the number of moves is less than 4, use the initial depth
        if (nMoves < 2) {
            currentDepth = initial_depth;
        } else {
            currentDepth = max_depth;
        }

        for (int col : board.getValidSteps()) {
            Board copy = new Board(board);
            copy.step(aiPlayer, col);
            int score = minimax(copy, currentDepth, false);

            if (score > bestScore) {
                bestScore = score;
                bestCol = col;
            }
        }

        return bestCol;
    }

}