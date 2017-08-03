package com.maximcedaroff.soundrec;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by Daniel on 12/29/2014.
 */
public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
		implements OnDatabaseChangedListener {

	private static final String LOG_TAG = "FileViewerAdapter";

	private DBHelper mDatabase;

	RecordingItem item;
	Context mContext;
	LinearLayoutManager llm;

	public FileViewerAdapter(Context context, LinearLayoutManager linearLayoutManager) {
		super();
		mContext = context;
		mDatabase = new DBHelper(mContext);
		mDatabase.setOnDatabaseChangedListener(this);
		llm = linearLayoutManager;
	}

	@Override
	public void onBindViewHolder(final RecordingsViewHolder holder, int position) {

		item = getItem(position);
		long itemDuration = item.getLength();

		long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
				- TimeUnit.MINUTES.toSeconds(minutes);

		holder.vName.setText(item.getName());
		holder.fileSize.setText(item.getFileSize() + " kb");
		holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
		holder.vDateAdded.setText(
				DateUtils.formatDateTime(
						mContext,
						item.getTime(),
						DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_YEAR
				)
		);

		// define an on click listener to open PlaybackFragment
		holder.cardView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					PlaybackFragment playbackFragment =
							new PlaybackFragment().newInstance(getItem(holder.getPosition()));

					FragmentTransaction transaction = ((FragmentActivity) mContext)
							.getSupportFragmentManager()
							.beginTransaction();

					playbackFragment.show(transaction, "dialog_playback");

				} catch (Exception e) {
					Log.e(LOG_TAG, "exception", e);
				}
			}
		});

		holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				LayoutInflater inflater = LayoutInflater.from(mContext);
				View dialogView = inflater.inflate(R.layout.dialog_share, null);
				builder.setView(dialogView);
//				builder.setTitle(mContext.getString(R.string.dialog_title_options));
				TextView share = (TextView) dialogView.findViewById(R.id.share);
				TextView delete = (TextView) dialogView.findViewById(R.id.delete);
				TextView rename = (TextView) dialogView.findViewById(R.id.rename);
				setTypeface(share);
				setTypeface(delete);
				setTypeface(rename);
				share.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						shareFileDialog(holder.getAdapterPosition());
					}
				});
				delete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						deleteFileDialog(holder.getAdapterPosition());
					}
				});
				rename.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						renameFileDialog(holder.getAdapterPosition());
					}
				});
				builder.setCancelable(true);
				AlertDialog alert = builder.create();
				alert.show();

				return false;
			}
		});
	}

	private void setTypeface(TextView share) {
		Typeface myTypeface = Typeface.createFromAsset(mContext.getAssets(), "punk kid.ttf");
		share.setTypeface(myTypeface);
	}

	@Override
	public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View itemView = LayoutInflater.
				from(parent.getContext()).
				inflate(R.layout.card_view, parent, false);

		mContext = parent.getContext();

		return new RecordingsViewHolder(itemView);
	}

	public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
		protected TextView vName, fileSize;
		protected TextView vLength;
		protected TextView vDateAdded;
		protected View cardView;

		public RecordingsViewHolder(View v) {
			super(v);
			vName = (TextView) v.findViewById(R.id.file_name_text);
			fileSize = (TextView) v.findViewById(R.id.file_size);
			vLength = (TextView) v.findViewById(R.id.file_length_text);
			vDateAdded = (TextView) v.findViewById(R.id.file_date_added_text);
			cardView = v.findViewById(R.id.card_view);
		}
	}

	@Override
	public int getItemCount() {
		return mDatabase.getCount();
	}

	public RecordingItem getItem(int position) {
		return mDatabase.getItemAt(position);
	}

	@Override
	public void onNewDatabaseEntryAdded() {
		//item added to top of the list
		notifyItemInserted(getItemCount() - 1);
		llm.scrollToPosition(getItemCount() - 1);
	}

	@Override
	//TODO
	public void onDatabaseEntryRenamed() {

	}

	public void remove(int position) {
		//remove item from database, recyclerview and storage

		//delete file from storage
		File file = new File(getItem(position).getFilePath());
		file.delete();

		Toast.makeText(
				mContext,
				String.format(
						mContext.getString(R.string.toast_file_delete),
						getItem(position).getName()
				),
				Toast.LENGTH_SHORT
		).show();

		mDatabase.removeItemWithId(getItem(position).getId());
		notifyItemRemoved(position);
	}

	//TODO
	public void removeOutOfApp(String filePath) {
		//user deletes a saved recording out of the application through another application
	}

	public void rename(int position, String name) {
		//rename a file

		String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFilePath += "/SoundRecorder/" + name;
		File f = new File(mFilePath);

		if (f.exists() && !f.isDirectory()) {
			//file name is not unique, cannot rename file.
			Toast.makeText(mContext,
					String.format(mContext.getString(R.string.toast_file_exists), name),
					Toast.LENGTH_SHORT).show();

		} else {
			//file name is unique, rename file
			File oldFilePath = new File(getItem(position).getFilePath());
			oldFilePath.renameTo(f);
			mDatabase.renameItem(getItem(position), name, mFilePath);
			notifyItemChanged(position);
		}
	}

	public void shareFileDialog(int position) {
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
		shareIntent.setType("audio/mp4");
		mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));
	}

	public void renameFileDialog(final int position) {
		final AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.dialog_rename_file, null);

		final EditText input = (EditText) view.findViewById(R.id.new_name);
		setTypeface((TextView) view.findViewById(R.id.title));
		final Button noBtn = (Button) view.findViewById(R.id.noBtn);
		final Button renameBtn = (Button) view.findViewById(R.id.renameBtn);
		setTypeface(noBtn);
		setTypeface(renameBtn);
		renameFileBuilder.setCancelable(true);

		renameFileBuilder.setView(view);
		final AlertDialog alert = renameFileBuilder.create();
		noBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alert.cancel();
			}
		});
		renameBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					String value = input.getText().toString().trim() + ".wav";
					rename(position, value);

				} catch (Exception e) {
					Log.e(LOG_TAG, "exception", e);
				}

				alert.cancel();
			}
		});
		alert.show();
	}

	public void deleteFileDialog(final int position) {
		// File delete confirm
		AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
		confirmDelete.setCancelable(true);
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.dialog_delete_file, null);
		confirmDelete.setView(view);

		setTypeface((TextView) view.findViewById(R.id.title));
		final AlertDialog alert = confirmDelete.create();
		final Button noBtn = (Button) view.findViewById(R.id.noBtn);
		final Button deleteBtn = (Button) view.findViewById(R.id.yesBtn);
		setTypeface(noBtn);
		setTypeface(deleteBtn);
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					//remove item from database, recyclerview, and storage
					remove(position);

				} catch (Exception e) {
					Log.e(LOG_TAG, "exception", e);
				}
				alert.cancel();
			}
		});
		noBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alert.cancel();
			}
		});
		alert.show();
	}
}
