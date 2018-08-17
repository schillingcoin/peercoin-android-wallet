/*
 * Copyright 2013-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.schillingcoin.schillingcoin_android_wallet.ui;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.schillingcoin.schillingcoinj.core.Coin;
import com.schillingcoin.schillingcoinj.utils.ExchangeRate;
import com.schillingcoin.schillingcoinj.utils.Fiat;

import android.view.View;
import com.schillingcoin.schillingcoin_android_wallet.ui.CurrencyAmountView.Listener;

/**
 * @author Andreas Schildbach
 */
public final class CurrencyCalculatorLink
{
	private final CurrencyAmountView OESAmountView;
	private final CurrencyAmountView localAmountView;

	private Listener listener = null;
	private boolean enabled = true;
	private ExchangeRate exchangeRate = null;
	private boolean exchangeDirection = true;

	private final CurrencyAmountView.Listener OESAmountViewListener = new CurrencyAmountView.Listener()
	{
		@Override
		public void changed()
		{
			if (OESAmountView.getAmount() != null)
				setExchangeDirection(true);
			else if(localAmountView != null)
				localAmountView.setHint(null);

			if (listener != null)
				listener.changed();
		}

		@Override
		public void focusChanged(final boolean hasFocus)
		{
			if (listener != null)
				listener.focusChanged(hasFocus);
		}
	};

	private final CurrencyAmountView.Listener localAmountViewListener = new CurrencyAmountView.Listener()
	{
		@Override
		public void changed()
		{
			if (localAmountView != null && localAmountView.getAmount() != null)
				setExchangeDirection(false);
			else
				OESAmountView.setHint(null);

			if (listener != null)
				listener.changed();
		}

		@Override
		public void focusChanged(final boolean hasFocus)
		{
			if (listener != null)
				listener.focusChanged(hasFocus);
		}
	};

	public CurrencyCalculatorLink(@Nonnull final CurrencyAmountView OESAmountView, final CurrencyAmountView localAmountView)
	{
		this.OESAmountView = OESAmountView;
		this.OESAmountView.setListener(OESAmountViewListener);

		this.localAmountView = localAmountView;

		if(this.localAmountView != null)
			this.localAmountView.setListener(localAmountViewListener);

		update();
	}

	public void setListener(@Nullable final Listener listener)
	{
		this.listener = listener;
	}

	public void setEnabled(final boolean enabled)
	{
		this.enabled = enabled;

		update();
	}

	public void setExchangeRate(@Nonnull final ExchangeRate exchangeRate)
	{
		this.exchangeRate = exchangeRate;

		update();
	}

	@CheckForNull
	public Coin getAmount()
	{
		if (exchangeDirection)
		{
			return (Coin) OESAmountView.getAmount();
		}
		else if (exchangeRate != null && localAmountView != null)
		{
			final Fiat localAmount = (Fiat) localAmountView.getAmount();
			try
			{
				return localAmount != null ? exchangeRate.fiatToCoin(localAmount) : null;
			}
			catch (ArithmeticException x)
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	public boolean hasAmount()
	{
		return getAmount() != null;
	}

	private void update()
	{
		OESAmountView.setEnabled(enabled);

		if (exchangeRate != null && localAmountView != null)
		{
			localAmountView.setEnabled(enabled);
			localAmountView.setCurrencySymbol(exchangeRate.fiat.currencyCode);

			if (exchangeDirection)
			{
				final Coin OESAmount = (Coin) OESAmountView.getAmount();
				if (OESAmount != null)
				{
					localAmountView.setAmount(null, false);
					localAmountView.setHint(exchangeRate.coinToFiat(OESAmount));
					OESAmountView.setHint(null);
				}
			}
			else
			{
				final Fiat localAmount = (Fiat) localAmountView.getAmount();
				if (localAmount != null)
				{
					localAmountView.setHint(null);
					OESAmountView.setAmount(null, false);
					try
					{
						OESAmountView.setHint(exchangeRate.fiatToCoin(localAmount));
					}
					catch (final ArithmeticException x)
					{
						OESAmountView.setHint(null);
					}
				}
			}
		}
		else
		{
			if(localAmountView != null) {
				localAmountView.setEnabled(false);
				localAmountView.setHint(null);
			}
			OESAmountView.setHint(null);
		}
	}

	public void setExchangeDirection(final boolean exchangeDirection)
	{
		this.exchangeDirection = exchangeDirection;

		update();
	}

	public boolean getExchangeDirection()
	{
		return exchangeDirection;
	}

	public View activeTextView()
	{
		if (exchangeDirection)
			return OESAmountView.getTextView();
		else if(localAmountView != null)
			return localAmountView.getTextView();
		else
			return null;
	}

	public void requestFocus()
	{
		activeTextView().requestFocus();
	}

	public void setOESAmount(@Nonnull final Coin amount)
	{
		final Listener listener = this.listener;
		this.listener = null;

		OESAmountView.setAmount(amount, true);

		this.listener = listener;
	}

	public void setNextFocusId(final int nextFocusId)
	{
		OESAmountView.setNextFocusId(nextFocusId);
		if(localAmountView != null)
			localAmountView.setNextFocusId(nextFocusId);
	}
}
