package com.topkishmopix.peak.setting.shop.report.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.utils.RialFormatter
import com.fanap.corepos.utils.aryan.AryanUtils
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.TransactionItemBinding

class TransactionAdapter(val data: List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class TransactionHolder(v: View) : RecyclerView.ViewHolder(v) {
        var binding: TransactionItemBinding = TransactionItemBinding.bind(v)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionHolder(v)
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val item: Transaction = data[position]
        holder.binding.txtStan.text = item.stan
        holder.binding.txtAmount.text = RialFormatter.format(item.amount?: "0") + " ریال"
        holder.binding.txtDateTime.text = AryanUtils.getShamsiDateFromString(item.date?.take(4) ?: "0000") + " " + AryanUtils.getTimeForReceipt(item.date?.takeLast(6) ?: "000000")
        if (item.rrn.isNullOrEmpty())
            item.rrn = "-"
        holder.binding.txtTitle.text = when (item.processCode) {
            "000000" -> "خرید"
            "170000" -> "پرداخت قبض"
            "220000" -> "شارژ مستقیم"
            "180000" -> "شارژ وچر"
            else -> ""
        }
    }
}

