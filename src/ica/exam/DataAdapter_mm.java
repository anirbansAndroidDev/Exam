package ica.exam;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

public class DataAdapter_mm extends ArrayAdapter<DataObject_mm> {
	private List<DataObject_mm> list;
	private Activity context;
	private String[] aAccount = null;

	public DataAdapter_mm(Activity context, List<DataObject_mm> list) {
		super(context, R.layout.question_mm_row, list);
		this.context = context;
		this.list = list;

		boolean isUnique;
		int unicksize = 0;

		for (int idx = 0; idx < list.size(); idx++) {
			isUnique = true;
			for (int ctr = idx + 1; ctr < list.size(); ctr++) {
				if (list.get(ctr).getAnswerAttribute1()
						.equals(list.get(idx).getAnswerAttribute1()) == true) {
					isUnique = false;
					break;
				}
			}
			if (isUnique == true) {
				unicksize++;
			}
		}

		int unicqueid = 0;

		aAccount = new String[unicksize + 1];
		aAccount[0] = "Select...";
		for (int idx = 0; idx < list.size(); idx++) {
			isUnique = true;
			for (int ctr = idx + 1; ctr < list.size(); ctr++) {
				if (list.get(ctr).getAnswerAttribute1()
						.equals(list.get(idx).getAnswerAttribute1()) == true) {
					isUnique = false;
					break;
				}
			}

			if (isUnique == true) {
				unicqueid++;
				aAccount[unicqueid] = list.get(idx).getAnswerAttribute1();
			}
		}
	}

	static class ViewHolder {
		protected TextView rowid;
		protected Button answerattribute1;
		protected EditText answerattribute2;
		protected EditText answerattribute3;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		final int iposition = position;
		if (convertView == null) {
			LayoutInflater inflator = ((Activity) context).getLayoutInflater();
			view = inflator.inflate(R.layout.question_mm_row, null);
			ViewHolder viewholder = new ViewHolder();
			viewholder.rowid = (TextView) view.findViewById(R.id.tvRow);
			viewholder.answerattribute1 = (Button) view
					.findViewById(R.id.btAnswerAttribute1);
			viewholder.answerattribute2 = (EditText) view
					.findViewById(R.id.etAnswerAttribute2);
			viewholder.answerattribute3 = (EditText) view
					.findViewById(R.id.etAnswerAttribute3);

			view.setTag(viewholder);
		} else {
			view = convertView;
			((ViewHolder) view.getTag()).rowid.setText(list.get(position)
					.getRowId());
			((ViewHolder) view.getTag()).answerattribute1
					.setText(getAccountName(list.get(position).getAnsweredId()));
			((ViewHolder) view.getTag()).answerattribute2.setText(list.get(
					position).getAnswerAttribute2());
			((ViewHolder) view.getTag()).answerattribute3.setText(list.get(
					position).getAnswerAttribute3());
		}

		final ViewHolder holder = (ViewHolder) view.getTag();
		holder.rowid.setText(list.get(position).getRowId());
		holder.answerattribute1.setText(getAccountName(list.get(position)
				.getAnsweredId()));
		holder.answerattribute2.setText(list.get(position)
				.getAnswerAttribute2());
		holder.answerattribute3.setText(list.get(position)
				.getAnswerAttribute3());

		holder.answerattribute1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder adLedger;
				final View vwLedger;

				LayoutInflater factory = LayoutInflater.from(context);
				vwLedger = factory.inflate(R.layout.ledger, null);
				adLedger = new AlertDialog.Builder(context);

				Spinner spAccount = (Spinner) vwLedger
						.findViewById(R.id.dgspAC);
				ArrayAdapter<CharSequence> adapterAccount = new ArrayAdapter<CharSequence>(
						context, R.layout.spinnerlayout, aAccount);
				spAccount.setAdapter(adapterAccount);

				for (int idx = 0; idx < aAccount.length; idx++) {
					if (getAccountName(list.get(iposition).getAnsweredId())
							.endsWith(aAccount[idx].toString())) {
						spAccount.setSelection(idx);
						break;
					}
				}

				EditText etAmount = (EditText) vwLedger
						.findViewById(R.id.dgetAmount);
				if (list.get(iposition).getAnswerAttribute2().toString()
						.length() > 0) {
					RadioButton rbDr = (RadioButton) vwLedger
							.findViewById(R.id.rbDr);
					rbDr.setChecked(true);
					etAmount.setText(list.get(iposition).getAnswerAttribute2());
				}

				if (list.get(iposition).getAnswerAttribute3().toString()
						.length() > 0) {
					RadioButton rbCr = (RadioButton) vwLedger
							.findViewById(R.id.rbCr);
					rbCr.setChecked(true);
					etAmount.setText(list.get(iposition).getAnswerAttribute3());
				}

				adLedger.setIcon(R.drawable.dlg_ledger);
				adLedger.setTitle("Ledger entry");
				adLedger.setMessage("To enter the ledger data please fullup the following fields and press Ok to save and Cancel to exit.");
				adLedger.setView(vwLedger);
				adLedger.setPositiveButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						})
						
						.setNegativeButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Spinner spAccount = (Spinner) vwLedger
										.findViewById(R.id.dgspAC);
								if (spAccount == null)
									return;
								if (spAccount.getSelectedItem().toString()
										.equals("Select...") == true) {
									Toast.makeText(
											context,
											"Please select the account before compliting this ledger entry.",
											Toast.LENGTH_LONG).show();
									return;
								} else {
									list.get(iposition)
											.setAnsweredId(
													spAccount
															.getSelectedItemPosition());
									holder.answerattribute1.setText(getAccountName(spAccount
											.getSelectedItemPosition()));
								}

								RadioButton rbDr = (RadioButton) vwLedger
										.findViewById(R.id.rbDr);
								if (rbDr == null)
									return;

								EditText tvAmount = (EditText) vwLedger
										.findViewById(R.id.dgetAmount);
								if (tvAmount == null)
									return;
								if (tvAmount.getText().toString().length() == 0) {
									Toast.makeText(
											context,
											"Please enter the amount before compliting this ledger entry.",
											Toast.LENGTH_LONG).show();
									return;
								}

								if (rbDr.isChecked()) {
									if (tvAmount != null) {
										list.get(iposition)
												.setAnswerAttribute2(
														tvAmount.getText()
																.toString());
										list.get(iposition)
												.setAnswerAttribute3("");
									}
								} else {
									if (tvAmount != null) {
										list.get(iposition)
												.setAnswerAttribute3(
														tvAmount.getText()
																.toString());
										list.get(iposition)
												.setAnswerAttribute2("");
									}
								}

								holder.answerattribute2.setText(list.get(
										iposition).getAnswerAttribute2());
								holder.answerattribute3.setText(list.get(
										iposition).getAnswerAttribute3());

								return;
							}
						});

				adLedger.create();
				adLedger.show();
			}
		});

		return view;
	}

	private String getAccountName(int position) {
		String name = null;

		for (int idx = 0; idx < aAccount.length; idx++) {
			if (position == idx) {
				name = aAccount[idx];
				break;
			}
		}
		return name;
	}
}