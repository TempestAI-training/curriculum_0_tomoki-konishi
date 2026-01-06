import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


public class ThreeDMS_2 extends JFrame {
    
    private GamePanel gamePanel;

    // 初期設定
    private int currentSize = 5;
    private int currentLayers = 4;
    private int currentMines = 15;

    public ThreeDMS_2() {
        setTitle("3D Minesweeper");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 初期設定でゲームパネルを作成
        gamePanel = new GamePanel(currentSize, currentLayers, currentMines);
        add(gamePanel);
        

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ThreeDMS_2 game = new ThreeDMS_2();
            if (game.showSettingsDialog("Start New Game")) {
                game.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }

  
    public boolean showSettingsDialog(String title) {
        JTextField sizeField = new JTextField(String.valueOf(currentSize));
        JTextField layerField = new JTextField(String.valueOf(currentLayers));
        JTextField minesField = new JTextField(String.valueOf(currentMines));
        
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.add(new JLabel("Grid Size (A*A):"));
        inputPanel.add(sizeField);
        inputPanel.add(new JLabel("Layers (B):"));
        inputPanel.add(layerField);
        inputPanel.add(new JLabel("Mines:"));
        inputPanel.add(minesField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, 
                 title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                int s = Integer.parseInt(sizeField.getText());
                int l = Integer.parseInt(layerField.getText());
                int m = Integer.parseInt(minesField.getText());
                
                // 値の検証と制限
                s = Math.max(2, Math.min(20, s));
                l = Math.max(1, Math.min(20, l));
                m = Math.max(1, Math.min(s * s * l - 1, m));
                
                this.currentSize = s;
                this.currentLayers = l;
                this.currentMines = m;
                
                // ゲームパネルに新しい設定を適用してリセット
                gamePanel.startNewGame(s, l, m);
                
                // タイトル更新
                setTitle(String.format("3DMS - %dx%dx%d (%d Mines)", s, l, s, m));
                return true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Using previous settings.");
                return true; // 続行
            }
        }
        return false;
    }

    // --- ゲームのメインロジックと描画を行うパネル ---
    class GamePanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
        
        private int gridSize;
        private int layerCount;
        private int totalMines;

        // 見た目の設定: 隙間をなくすため SPACING = BLOCK_SIZE とする
        private static final int BLOCK_SIZE = 50; 
        
        private Block[][][] blocks;
        
        // 視点角度を元の値に戻す (45度単位での計算となっていたため数値も戻す)
        private double angleX = 45; 
        private double angleY = 45; 
        
        private int lastMouseX, lastMouseY;
        private boolean gameOver = false;
        private boolean gameWon = false;
        
        private int currentLayer = 0; 
        private List<Block> drawOrder = new ArrayList<>();
        private Block hoveredBlock = null;

        public GamePanel(int size, int layers, int mines) {
            setBackground(new Color(30, 30, 30));
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
            setFocusable(true); 
            
            // キー操作
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_R:
                            SwingUtilities.invokeLater(() -> showSettingsDialog("Reset Game Settings"));
                            break;
                        case KeyEvent.VK_UP:
                            changeLayer(-1); 
                            break;
                        case KeyEvent.VK_DOWN:
                            changeLayer(1);
                            break;
                        case KeyEvent.VK_LEFT:
                            rotateLayer(false); // 反時計回り
                            break;
                        case KeyEvent.VK_RIGHT:
                            rotateLayer(true); // 時計回り
                            break;
                        case KeyEvent.VK_H:
                            provideHint(); // ヒント
                            break;
                    }
                }
            });
            
            startNewGame(size, layers, mines);
        }

        public void changeLayer(int delta) {
            int newLayer = currentLayer + delta;
            if (newLayer >= 0 && newLayer < layerCount) {
                currentLayer = newLayer;
                repaint();
            }
        }

        /**
         * 現在の層を90度回転させる
         * @param clockwise trueなら時計回り
         */
        public void rotateLayer(boolean clockwise) {
            if (gameOver) return;

            // 回転後の一時配列
            Block[][] newLayer = new Block[gridSize][gridSize];
            int y = currentLayer;

            for (int x = 0; x < gridSize; x++) {
                for (int z = 0; z < gridSize; z++) {
                    Block b = blocks[x][y][z];
                    int newX, newZ;

                    // 回転座標計算
                    if (clockwise) {
                        newX = gridSize - 1 - z;
                        newZ = x;
                    } else {
                        newX = z;
                        newZ = gridSize - 1 - x;
                    }
                    
                    // ブロック内部の座標情報を更新
                    b.gridX = newX;
                    b.gridZ = newZ;
                    
                    // ワールド座標(描画用)も再計算
                    b.worldX = (newX - (gridSize - 1) / 2.0) * BLOCK_SIZE;
                    b.worldZ = (newZ - (gridSize - 1) / 2.0) * BLOCK_SIZE;
                    
                    newLayer[newX][newZ] = b;
                }
            }

            // メイン配列を更新
            for (int x = 0; x < gridSize; x++) {
                for (int z = 0; z < gridSize; z++) {
                    blocks[x][y][z] = newLayer[x][z];
                }
            }

            //全ブロックの数字を再計算する
            calculateNumbers();
            repaint();
        }

        /**
         * ヒント機能: 未開放の安全なマスを一つ開ける
         */
        public void provideHint() {
            if (gameOver) return;

            List<Block> safeBlocks = new ArrayList<>();
            
            // まず現在の層から候補を探す
            for (int x = 0; x < gridSize; x++) {
                for (int z = 0; z < gridSize; z++) {
                    Block b = blocks[x][currentLayer][z];
                    if (!b.isMine && !b.isRevealed && !b.isFlagged) {
                        safeBlocks.add(b);
                    }
                }
            }
            
            // 現在の層になければ他の層から探す
            if (safeBlocks.isEmpty()) {
                for (int y = 0; y < layerCount; y++) {
                    if (y == currentLayer) continue;
                    for (int x = 0; x < gridSize; x++) {
                        for (int z = 0; z < gridSize; z++) {
                            Block b = blocks[x][y][z];
                            if (!b.isMine && !b.isRevealed && !b.isFlagged) {
                                safeBlocks.add(b);
                            }
                        }
                    }
                }
            }

            // 候補があればランダムに1つ開ける
            if (!safeBlocks.isEmpty()) {
                Random rand = new Random();
                Block target = safeBlocks.get(rand.nextInt(safeBlocks.size()));
                reveal(target.gridX, target.gridY, target.gridZ);
                checkWinCondition();
                repaint();
            }
        }

        // 新しい設定でゲームを再開する
        public void startNewGame(int size, int layers, int mines) {
            this.gridSize = size;
            this.layerCount = layers;
            this.totalMines = mines;
            
            blocks = new Block[gridSize][layerCount][gridSize];
            drawOrder.clear();
            gameOver = false;
            gameWon = false;
            currentLayer = 0;

            // ブロックの初期化
            for (int x = 0; x < gridSize; x++) {
                for (int y = 0; y < layerCount; y++) {
                    for (int z = 0; z < gridSize; z++) {
                        // 隙間をなくすため、座標計算には BLOCK_SIZE をそのまま使う
                        double ox = (x - (gridSize - 1) / 2.0) * BLOCK_SIZE;
                        double oy = (y - (layerCount - 1) / 2.0) * BLOCK_SIZE;
                        double oz = (z - (gridSize - 1) / 2.0) * BLOCK_SIZE;
                        
                        Block b = new Block(x, y, z, ox, oy, oz);

                        // 四隅のマーカーIDを設定
                        if (x == 0 && z == 0) b.cornerId = 1; // 赤 (0,0)
                        else if (x == gridSize - 1 && z == 0) b.cornerId = 2; // 青 (Max, 0)
                        else if (x == gridSize - 1 && z == gridSize - 1) b.cornerId = 3; // 緑 (Max, Max)
                        else if (x == 0 && z == gridSize - 1) b.cornerId = 4; // 黄 (0, Max)

                        blocks[x][y][z] = b;
                        drawOrder.add(b);
                    }
                }
            }

            placeMines();
            calculateNumbers();
            repaint();
        }

        private void placeMines() {
            Random rand = new Random();
            int placed = 0;
            // 安全策: 全マスが地雷にならないように
            int limit = gridSize * layerCount * gridSize;
            if (totalMines >= limit) totalMines = limit - 1;

            while (placed < totalMines) {
                int x = rand.nextInt(gridSize);
                int y = rand.nextInt(layerCount);
                int z = rand.nextInt(gridSize);
                if (!blocks[x][y][z].isMine) {
                    blocks[x][y][z].isMine = true;
                    placed++;
                }
            }
        }

        private void calculateNumbers() {
            for (int x = 0; x < gridSize; x++) {
                for (int y = 0; y < layerCount; y++) {
                    for (int z = 0; z < gridSize; z++) {
                        if (blocks[x][y][z].isMine) continue;
                        blocks[x][y][z].neighborMines = countNeighborMines(x, y, z);
                    }
                }
            }
        }

        // 修正: 「同じ層の8近傍」＋「真上」＋「真下」のみをカウントする（斜め上下は含めない）
        private int countNeighborMines(int cx, int cy, int cz) {
            int count = 0;
            
            // 1. 同じ層 (y == cy) の周囲8マス (xz平面)
            for (int x = cx - 1; x <= cx + 1; x++) {
                for (int z = cz - 1; z <= cz + 1; z++) {
                    if (x == cx && z == cz) continue; // 自分自身を除く
                    if (isValid(x, cy, z) && blocks[x][cy][z].isMine) count++;
                }
            }

            // 2. 真上 (cy - 1) - 斜め上は含めない
            if (isValid(cx, cy - 1, cz) && blocks[cx][cy - 1][cz].isMine) count++;

            // 3. 真下 (cy + 1) - 斜め下は含めない
            if (isValid(cx, cy + 1, cz) && blocks[cx][cy + 1][cz].isMine) count++;

            return count;
        }

        private boolean isValid(int x, int y, int z) {
            return x >= 0 && x < gridSize && y >= 0 && y < layerCount && z >= 0 && z < gridSize;
        }

        // 修正: 連鎖開放も「同じ層の8近傍」＋「真上」＋「真下」のみに広がる
        private void reveal(int x, int y, int z) {
            if (!isValid(x, y, z)) return;
            Block b = blocks[x][y][z];
            if (b.isRevealed || b.isFlagged) return;

            b.isRevealed = true;

            if (b.neighborMines == 0 && !b.isMine) {
                // 同じ層の周囲へ広がる
                for (int nx = x - 1; nx <= x + 1; nx++) {
                    for (int nz = z - 1; nz <= z + 1; nz++) {
                        if (nx == x && nz == z) continue;
                        reveal(nx, y, nz);
                    }
                }
                // 真上と真下へ広がる
                reveal(x, y - 1, z);
                reveal(x, y + 1, z);
            }
        }

        private void checkWinCondition() {
            int unrevealedSafeBlocks = 0;
            for (Block b : drawOrder) {
                if (!b.isMine && !b.isRevealed) {
                    unrevealedSafeBlocks++;
                }
            }
            if (unrevealedSafeBlocks == 0) {
                gameWon = true;
                gameOver = true;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 背景
            g2.setPaint(new GradientPaint(0, 0, new Color(40, 40, 40), 0, getHeight(), new Color(10, 10, 10)));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // 投影計算
            for (Block b : drawOrder) {
                b.project(getWidth(), getHeight(), angleX, angleY, BLOCK_SIZE);
            }
            Collections.sort(drawOrder, Comparator.comparingDouble((Block b) -> b.projZ).reversed());

            hoveredBlock = null; 
            Point mouseP = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(mouseP, this);

            for (Block b : drawOrder) {
                // 変更: drawメソッドに現在の層(currentLayer)を渡すように変更
                b.draw(g2, currentLayer);

                // マウス判定はターゲット層のみ
                if (b.gridY == currentLayer && b.contains(mouseP.x, mouseP.y)) {
                    if (!b.isRevealed || b.neighborMines > 0 || b.isMine || (b.isRevealed && b.isEmpty())) {
                        hoveredBlock = b;
                    }
                }
            }

            // ホバー枠
            if (hoveredBlock != null && !gameOver && hoveredBlock.gridY == currentLayer) {
                g2.setColor(new Color(255, 255, 255, 150));
                g2.setStroke(new BasicStroke(2));
                Rectangle r = (Rectangle)hoveredBlock.shape;
                g2.drawRect(r.x+1, r.y+1, r.width-2, r.height-2);
            }

            // HUD
            drawHUD(g2);
            // ロゴ描画
            drawLogo(g2);
        }

        private void drawHUD(Graphics2D g2) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            
            int y = 30;
            g2.drawString("Layer: " + (currentLayer + 1) + " / " + layerCount + " (↑/↓ to change)", 20, y);
            y += 25;
            g2.drawString("Mines: " + totalMines, 20, y);
            y += 25;
            g2.drawString("[←/→] Rotate Layer", 20, y);
            y += 25;
            g2.drawString("[H] Hint (Reveal 1 Safe)", 20, y);
            y += 25;
            g2.drawString("[R] Settings & Reset", 20, y);

            if (gameOver) {
                g2.setFont(new Font("SansSerif", Font.BOLD, 50));
                String msg = gameWon ? "YOU WIN!" : "GAME OVER";
                Color c = gameWon ? Color.GREEN : Color.RED;
                
                FontMetrics fm = g2.getFontMetrics();
                int msgW = fm.stringWidth(msg);
                int msgX = (getWidth() - msgW) / 2;
                int msgY = getHeight() / 2;
                
                g2.setColor(Color.BLACK);
                g2.drawString(msg, msgX + 2, msgY + 2);
                g2.setColor(c);
                g2.drawString(msg, msgX, msgY);
                
                g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
                String sub = "Press 'R' to play again";
                int subW = g2.getFontMetrics().stringWidth(sub);
                g2.setColor(Color.WHITE);
                g2.drawString(sub, (getWidth() - subW) / 2, msgY + 40);
            }
        }

        private void drawLogo(Graphics2D g2) {
            // 右上にロゴを描画
            String text = "3D MINESWEEPER";
            // システムにImpactがあれば使うが、なければSansSerif
            Font font = new Font("Impact", Font.BOLD, 36);
            if (!font.getFamily().equals("Impact")) {
                font = new Font("SansSerif", Font.BOLD, 36);
            }
            
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(text);
            int h = fm.getAscent();
            
            int x = getWidth() - w - 20;
            int y = 50;

            // 影
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(text, x + 3, y + 3);
            
            // 本体（グラデーション）
            GradientPaint gp = new GradientPaint(x, y - h, new Color(0, 200, 255), x, y, new Color(0, 100, 200));
            g2.setPaint(gp);
            
            // アウトライン描画のためのTextLayout
            TextLayout tl = new TextLayout(text, font, g2.getFontRenderContext());
            AffineTransform transform = new AffineTransform();
            transform.setToTranslation(x, y);
            Shape shape = tl.getOutline(transform);
            
            g2.fill(shape);
            
            // 白い枠線
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(shape);
        }

        // --- Mouse Events ---
        @Override
        public void mousePressed(MouseEvent e) {
            requestFocusInWindow();
            lastMouseX = e.getX();
            lastMouseY = e.getY();

            if (gameOver) return;

            if (hoveredBlock != null && hoveredBlock.gridY == currentLayer) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (!hoveredBlock.isFlagged && !hoveredBlock.isRevealed) {
                        if (hoveredBlock.isMine) {
                            hoveredBlock.isRevealed = true;
                            gameOver = true;
                        } else {
                            reveal(hoveredBlock.gridX, hoveredBlock.gridY, hoveredBlock.gridZ);
                            checkWinCondition();
                        }
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (!hoveredBlock.isRevealed) {
                        hoveredBlock.isFlagged = !hoveredBlock.isFlagged;
                    }
                }
                repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int dx = e.getX() - lastMouseX;
            int dy = e.getY() - lastMouseY;
            angleY += dx * 0.01;
            angleX += dy * 0.01;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {}
        @Override
        public void mouseReleased(MouseEvent e) {}
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
        @Override
        public void mouseMoved(MouseEvent e) { repaint(); }
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {}
    }

    // --- Block Class ---
    class Block {
        int gridX, gridY, gridZ;
        double worldX, worldY, worldZ;
        boolean isMine, isRevealed, isFlagged;
        int neighborMines;
        int projX, projY;
        double projZ;
        Shape shape;
        int cornerId = 0; // 0:なし, 1:赤(0,0), 2:青(Max,0), 3:緑(Max,Max), 4:黄(0,Max)

        public Block(int gx, int gy, int gz, double wx, double wy, double wz) {
            this.gridX = gx; this.gridY = gy; this.gridZ = gz;
            this.worldX = wx; this.worldY = wy; this.worldZ = wz;
        }
        
        public boolean isEmpty() { return !isMine && neighborMines == 0; }

        public void project(int width, int height, double angX, double angY, int baseSize) {
            // 回転
            double x1 = worldX * Math.cos(angY) - worldZ * Math.sin(angY);
            double z1 = worldX * Math.sin(angY) + worldZ * Math.cos(angY);
            double y2 = worldY * Math.cos(angX) - z1 * Math.sin(angX);
            double z2 = worldY * Math.sin(angX) + z1 * Math.cos(angX);

            this.projZ = z2;
            
            double scale = 600 / (600 + z2); 
            
            this.projX = (int) (width / 2 + x1 * scale);
            this.projY = (int) (height / 2 + y2 * scale);
            
            int size = (int) Math.ceil(baseSize * scale); 
            
            this.shape = new Rectangle(projX - size/2, projY - size/2, size, size);
        }

        public boolean contains(int x, int y) {
            return shape != null && shape.contains(x, y);
        }

        // 変更: 引数を boolean isTargetLayer から int targetLayer に変更し、透明度ロジックを改良
        public void draw(Graphics2D g, int targetLayer) {
            if (shape == null) return;
            Rectangle rect = (Rectangle) shape;
            
            boolean isTarget = (gridY == targetLayer);
            
            // 透明度設定
            int alpha;
            if (isTarget) {
                alpha = 255;
            } else if (gridY < targetLayer) {
                // ターゲット層より上（手前）にある層は、邪魔にならないよう極めて薄く表示
                alpha = 15; 
            } else {
                // ターゲット層より下（奥）にある層は、ある程度見えるように
                alpha = 40; 
            }
            
            if (!isRevealed) {
                // --- 未開示 ---
                if (isFlagged) {
                    g.setColor(new Color(255, 180, 0, alpha));
                    g.fill(rect);
                    g.setColor(new Color(200, 0, 0, alpha));
                    // 旗印
                    int m = rect.width / 4;
                    g.fillRect(rect.x + m, rect.y + m, rect.width - 2*m, rect.height - 2*m);
                } else {
                    // ブロックの色
                    int brightness = 100 + (int)(projZ / 8);
                    brightness = Math.max(40, Math.min(200, brightness));
                    
                    if (isTarget) {
                        g.setColor(new Color(100, 100, brightness + 20, alpha));
                    } else {
                        g.setColor(new Color(brightness, brightness, brightness, alpha));
                    }
                    g.fill(rect);
                }
                
                g.setColor(new Color(0, 0, 0, alpha));
                g.setStroke(new BasicStroke(1));
                g.draw(rect);

                // コーナーマーカー (薄く全体を色付け)
                if (cornerId > 0 && isTarget) {
                    drawCornerOverlay(g, rect, alpha);
                }

            } else {
                // --- 開示済み ---
                if (isMine) {
                    g.setColor(new Color(150, 0, 0, alpha));
                    g.fill(rect);
                    g.setColor(new Color(255, 50, 50, alpha));
                    g.fillOval(rect.x + rect.width/4, rect.y + rect.height/4, rect.width/2, rect.height/2);
                } else {
                    if (neighborMines > 0) {
                        g.setColor(new Color(230, 230, 230, alpha));
                    } else {
                        g.setColor(new Color(200, 255, 200, isTarget ? 150 : 30));
                    }
                    g.fill(rect);
                    
                    g.setColor(new Color(100, 100, 100, isTarget ? 100 : 20));
                    g.draw(rect);

                    if (neighborMines > 0 && isTarget) {
                        g.setColor(getNumberColor(neighborMines));
                        g.setFont(new Font("Arial", Font.BOLD, (int)(rect.width * 0.7)));
                        FontMetrics fm = g.getFontMetrics();
                        String txt = String.valueOf(neighborMines);
                        int tx = rect.x + (rect.width - fm.stringWidth(txt)) / 2;
                        int ty = rect.y + (rect.height + fm.getAscent()) / 2 - 4;
                        g.drawString(txt, tx, ty);
                    }

                    // 開示済みでもコーナーマーカーを表示（位置確認用）
                    if (cornerId > 0 && isTarget) {
                        drawCornerOverlay(g, rect, alpha);
                    }
                }
            }
        }

        private void drawCornerOverlay(Graphics2D g, Rectangle rect, int alpha) {
            Color c;
            switch (cornerId) {
                case 1: c = Color.RED; break;
                case 2: c = Color.BLUE; break;
                case 3: c = Color.GREEN; break;
                case 4: c = Color.YELLOW; break;
                default: return;
            }
            // 薄く色を重ねる (alphaが低い場合も見やすく調整)
            int overlayAlpha = Math.min(60, alpha); // あまり濃くしすぎない
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), overlayAlpha));
            g.fill(rect);
        }

        private Color getNumberColor(int n) {
            switch (n) {
                case 1: return new Color(0, 0, 255);
                case 2: return new Color(0, 128, 0);
                case 3: return new Color(255, 0, 0);
                case 4: return new Color(0, 0, 128);
                case 5: return new Color(128, 0, 0);
                case 6: return new Color(0, 128, 128);
                case 7: return new Color(0, 0, 0);
                case 8: return new Color(128, 128, 128);
                default: return Color.BLACK;
            }
        }
    }
}