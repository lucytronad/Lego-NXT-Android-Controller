package com.example.lo52_project;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Class SimpleSurfaceView herited of surfaceView. Allows to draw a virtual World
 * and the user can interact with it through onTouchEvent
 * 
 * @author Lois Aubree & Lucie Boutou
 */
public class SimpleSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {

	private Robot mRobot;
	private Handler mHandler = new Handler();
	private ArrayList<Piece> mListPiece = new ArrayList<Piece>();
	/**
	 * Drawable grid in the background
	 */
	private MapDrawable mMap;
	/**
	 * The world configuration. It set of pieces.
	 * Indexes gives the position of a piece between each other 
	 */
	private Piece[][] mWorld;
	private WorldActivity.SelectedMode current_mode;
	/**
	 * Actual width of the world
	 */
	private int mWorldWidth = MapDrawable.NB_DEFAULT_ROWS;
	/**
	 * Actual height of the world
	 */
	private int mWorldHeight = MapDrawable.NB_DEFAULT_COLS;
	private Paint goal_paint = new Paint();
	private Paint path_paint = new Paint();
	private boolean isWorldLocked = false;
	private boolean isItemSelected = false;
	private PiecePathFinder mPathFinder;
	/**
	 * List of all pieces that are on the surfaceView. Even if these pieces are not placed
	 */
	private List<Piece> mPath;

	/**
	 * Runnable that permits to post the view as invalidate state
	 * It allows to refresh the Surfaceview
	 * The post function is send to the GUI Thread of Main activity 
	 */
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			invalidate();
			mHandler.postDelayed(this, 0);
		}
	};

	/**
	 * 
	 * @param Context of the application
	 * @constructor 
	 */
	public SimpleSurfaceView(Context context) {
		super(context);
		getHolder().addCallback(this);
		setBackgroundColor(Color.WHITE);
		goal_paint.setARGB(60, 0, 255, 0);
		path_paint.setARGB(255, 255, 0, 0);
		path_paint.setStrokeWidth(5.0f);

		/**
		 * Initialization of the robot 
		 */
		mRobot = new Robot((BitmapDrawable) getResources().getDrawable(
				R.drawable.robot));
		mRobot.getRobotGraphic().setPosX(100);
		mRobot.getRobotGraphic().setPosY(100);
		mRobot.getRobotGraphic().updateBounds();

		/**
		 * Initialization of grid map and the world map
		 */
		mMap = new MapDrawable(context);
		mWorldWidth = MapDrawable.NB_DEFAULT_ROWS;
		mWorldHeight = MapDrawable.NB_DEFAULT_COLS;
		mWorld = new Piece[mWorldWidth][mWorldWidth];

	}

	/**
	 * Draw the entire world. Pieces, background grid and Robot
	 *@param Canvas that permit to draw all, drawables things
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		mMap.draw(canvas);
		for (int i = 0; i < mListPiece.size(); i++) {
			Piece tmp = mListPiece.get(i);
			tmp.draw(canvas);
			if (tmp.isGoal()) {
				canvas.drawRect(tmp.getImage().getBounds(), goal_paint);
			}
		}
		if (mPath != null) {
			for (int i = 0; i < mPath.size() - 1; i++) {
				canvas.drawLine(mPath.get(i)._X, mPath.get(i)._Y,
						mPath.get(i + 1)._X, mPath.get(i + 1)._Y, path_paint);
			}
		}
		mRobot.drawOnPA(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mRunnable.run();
		mHandler.post(mRunnable);
		setWillNotDraw(false);
		mRobot.getRobotGraphic().updateBounds();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	/**
	 * @param Touch event on the SurfaceView
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		/**
		 * Robot can be moved when robot isn't placed the world is unlock.
		 * Action press of the robot
		 */
		if (current_mode.equals(WorldActivity.SelectedMode.MODE_VIRTUAL)) {
			if (isItemSelected == false
					&& mRobot.getRobotGraphic().isSelected() == false
					&& mRobot.getRobotGraphic().getImageBounds()
							.contains((int) x, (int) y)
					&& event.getAction() == MotionEvent.ACTION_DOWN) {
				mRobot.getRobotGraphic().setSelected(true);
				mRobot.getRobotGraphic().setPlaced(false);
				for (Piece tmp : mListPiece)
					tmp.setGoal(false);
				isItemSelected = true;
				isWorldLocked = false;
			}

			
			if (!isWorldLocked) {
				
				/**
				 * Action move of the robot
				 */
				if (mRobot.getRobotGraphic().isSelected()
						&& event.getAction() == MotionEvent.ACTION_MOVE) {
					mRobot.getRobotGraphic().setPosX(x);
					mRobot.getRobotGraphic().setPosY(y);
				}
				/**
				 * Action release of the robot
				 */
				if (mRobot.getRobotGraphic().isSelected()
						&& event.getAction() == MotionEvent.ACTION_UP) {
					mRobot.getRobotGraphic().setPosX(x);
					mRobot.getRobotGraphic().setPosY(y);

					for (int k = 0; k < mWorldWidth; k++)
						for (int l = 0; l < mWorldHeight; l++) {
							if (mMap.getCase(k, l).contains(
									(int) mRobot.getRobotGraphic().getPosX(),
									(int) mRobot.getRobotGraphic().getPosY())) {
								mRobot.getRobotGraphic().setImageBounds(
										mMap.getCase(k, l));
								mRobot.getRobotGraphic().setWorldPos(k, l);
								mRobot.getRobotGraphic().setPlaced(true);
								scanWorld();
								isWorldLocked = true;
							}
						}
					mRobot.getRobotGraphic().setSelected(false);
					isItemSelected = false;
				}
				
				/**
				 * Update image bounds of robot.
				 */
				if (!mRobot.getRobotGraphic().isPlaced())
					mRobot.getRobotGraphic().updateBounds();

				/**
				 * When the world is unlock pieces can be moved if there aren't placed.
				 */
				for (int i = 0; i < mListPiece.size(); i++) {
					Piece tmp = mListPiece.get(i);

					/**
					 * Press action on a piece
					 */
					if (!tmp.isPlaced
							&& isItemSelected == false
							&& tmp.isSelected == false
							&& tmp.getImage().getBounds()
									.contains((int) x, (int) y)
							&& event.getAction() == MotionEvent.ACTION_DOWN) {
						tmp.isSelected = true;
						isItemSelected = true;
						tmp.isPlaced = false;
					}
					/**
					 * Move action of a piece
					 */
					if (!tmp.isPlaced && tmp.isSelected
							&& event.getAction() == MotionEvent.ACTION_MOVE) {
						tmp._X = x;
						tmp._Y = y;
						tmp.scale_X = 1.0f;
						tmp.scale_Y = 1.0f;
					}
					/**
					 * Release action of a piece
					 */
					if (!tmp.isPlaced && tmp.isSelected
							&& event.getAction() == MotionEvent.ACTION_UP) {
						tmp._X = x;
						tmp._Y = y;
						tmp.isSelected = false;
						tmp.scale_X = 1.0f;
						tmp.scale_Y = 1.0f;
						for (int k = 0; k < mWorldWidth; k++)
							for (int l = 0; l < mWorldHeight; l++) {
								if (mMap.getCase(k, l).contains((int) tmp._X,
										(int) tmp._Y)) {
									if (mWorld[k][l] == null
											|| !mWorld[k][l].isPlaced) {
										tmp.getImage().setBounds(
												mMap.getCase(k, l));
										tmp.setIndices(k, l);
										mWorld[k][l] = tmp;
										tmp.isPlaced = true;
									}
								}
							}
						isItemSelected = false;
					}
					if (!tmp.isPlaced) {
						tmp.getImage().setBounds(
								(int) (tmp._X - Piece.PIECE_WIDTH / 2
										* tmp.scale_X),
								(int) (tmp._Y - Piece.PIECE_HEIGHT / 2
										* tmp.scale_Y),
								(int) (tmp._X + Piece.PIECE_WIDTH / 2
										* tmp.scale_X),
								(int) (tmp._Y + Piece.PIECE_HEIGHT / 2
										* tmp.scale_Y));
					}
				}
			}
		}
		/**
		 * If the robot is placed, the user can touch on a piece to set it as goal piece
		 */
		if (mRobot.getRobotGraphic().isPlaced()
				&& (current_mode
						.equals(WorldActivity.SelectedMode.MODE_MISSION) || current_mode
						.equals(WorldActivity.SelectedMode.MODE_VIRTUAL))) {
			for (int i = mListPiece.size() - 1; i > -1; i--) {
				Piece tmp = mListPiece.get(i);

				if (tmp.isPlaced
						&& tmp.getImage().getBounds()
								.contains((int) x, (int) y)
						&& event.getAction() == MotionEvent.ACTION_DOWN) {
					tmp.setGoal(true);
					for (Piece tmp2 : mListPiece)
						if (tmp2 != tmp)
							tmp2.setGoal(false);
					searchPath();
				}
			}
		}

		return true;
	}

	/**
	 * 
	 * @param image of the piece
	 * @param wall config of piece
	 */
	public void addPiece(BitmapDrawable bit, byte[] config) {
		Piece tmp = new Piece((BitmapDrawable) bit.getConstantState()
				.newDrawable(), config);
		mListPiece.add(tmp);
		tmp._X = 100;
		tmp._Y = 300;
		tmp.getImage().setBounds(
				(int) (tmp._X - Piece.PIECE_WIDTH / 2 * tmp.scale_X),
				(int) (tmp._Y - Piece.PIECE_HEIGHT / 2 * tmp.scale_Y),
				(int) (tmp._X + Piece.PIECE_WIDTH / 2 * tmp.scale_X),
				(int) (tmp._Y + Piece.PIECE_HEIGHT / 2 * tmp.scale_Y));
	}

	/**
	 * Scan the world to create fake pieces 
	 * Fake pieces are pieces with 
	 */
	private void scanWorld() {
		for (int i = 0; i < mWorldWidth; i++)
			for (int j = 0; j < mWorldHeight; j++) {
				if (mWorld[i][j] == null) {
					byte[] config = { Piece.VIDE, Piece.MUR_ON, Piece.MUR_ON,
							Piece.MUR_ON, Piece.MUR_ON };
					mWorld[i][j] = new Piece((BitmapDrawable) getResources()
							.getDrawable(R.drawable.vide_hdbg), config);
				}
			}
	}

	/**
	 * Launch the path searching and gives a list of pieces corresponding of the path found
	 * Places the robot to the goal piece if the path allows to join it
	 */
	private void searchPath() {
		mPathFinder = new PiecePathFinder(mWorld);
		mPath = mPathFinder.compute(mWorld[mRobot.getRobotGraphic()
				.getWorldPosX()][mRobot.getRobotGraphic().getWorldPosY()]);
		if (mPath != null) {
			if (!mPath.isEmpty()) {
				Piece last = (Piece) mPath.get(mPath.size() - 1);
				mRobot.getRobotGraphic().setWorldPos(last.getIndX(),
						last.getIndY());
				mRobot.getRobotGraphic().setImageBounds(
						new Rect(mMap.getCase(last.getIndX(), last.getIndY())));
			}
		}
	}

	public void setCurrentMode(WorldActivity.SelectedMode mode) {
		current_mode = mode;
	}

	
	/**
	 * Same as the add piece function. But add this piece on world set too.
	 * Gestion of the world map overflow
	 * @param piece
	 * @param row index
	 * @param cols index
	 */
	public boolean addPieceAt(Piece piece, int i, int j) {
		if (i < 0) {
			i = 0;
			mWorldWidth += 1;

			mMap.resize(mWorldWidth, mWorldHeight);
			for (Piece tmp : mListPiece) {
				tmp.setIndices(tmp.getIndX() + 1, tmp.getIndY());
			}

			mWorld = new Piece[mWorldWidth][mWorldHeight];
			mMap.resize(mWorldWidth, mWorldHeight);

			for (Piece tmp2 : mListPiece) {
				if (tmp2.getIndX() != -1 && tmp2.getIndY() != -1) {
					mWorld[tmp2.getIndX()][tmp2.getIndY()] = tmp2;
					tmp2.getImage().setBounds(
							mMap.getCase(tmp2.getIndX(), tmp2.getIndY()));
				}
			}
		}
		if (i >= mWorldWidth) {
			mWorldWidth += 1;
			mWorld = new Piece[mWorldWidth][mWorldHeight];
			mMap.resize(mWorldWidth, mWorldHeight);

			for (Piece tmp2 : mListPiece) {
				if (tmp2.getIndX() != -1 && tmp2.getIndY() != -1) {
					mWorld[tmp2.getIndX()][tmp2.getIndY()] = tmp2;
					tmp2.getImage().setBounds(
							mMap.getCase(tmp2.getIndX(), tmp2.getIndY()));
				}
			}
		}
		if (j < 0) {
			j = 0;
			mWorldHeight += 1;

			mMap.resize(mWorldWidth, mWorldHeight);
			for (Piece tmp : mListPiece) {
				tmp.setIndices(tmp.getIndX(), tmp.getIndY() + 1);
			}

			mWorld = new Piece[mWorldWidth][mWorldHeight];
			mMap.resize(mWorldWidth, mWorldHeight);

			for (Piece tmp2 : mListPiece) {
				if (tmp2.getIndX() != -1 && tmp2.getIndY() != -1) {
					mWorld[tmp2.getIndX()][tmp2.getIndY()] = tmp2;
					tmp2.getImage().setBounds(
							mMap.getCase(tmp2.getIndX(), tmp2.getIndY()));
				}
			}

			if (j >= mWorldHeight) {
				mWorldHeight += 1;
				mWorld = new Piece[mWorldWidth][mWorldHeight];
				mMap.resize(mWorldWidth, mWorldHeight);

				for (Piece tmp2 : mListPiece) {
					if (tmp2.getIndX() != -1 && tmp2.getIndY() != -1) {
						mWorld[tmp2.getIndX()][tmp2.getIndY()] = tmp2;
						tmp2.getImage().setBounds(
								mMap.getCase(tmp2.getIndX(), tmp2.getIndY()));
					}
				}
			}
		}
		mListPiece.add(piece);
		piece.getImage().setBounds(mMap.getCase(i, j));
		piece.setIndices(i, j);
		mWorld[i][j] = piece;
		piece.isPlaced = true;
		setRobotAt(i, j);

		return true;
	}

	/**
	 * Set the robot at a particular position
	 * @param row index
	 * @param cols index
	 */
	public void setRobotAt(int i, int j) {
		mRobot.getRobotGraphic().setImageBounds(mMap.getCase(i, j));
		mRobot.getRobotGraphic().setWorldPos(i, j);
		mRobot.getRobotGraphic().setPlaced(true);
		isWorldLocked = true;
	}
}
