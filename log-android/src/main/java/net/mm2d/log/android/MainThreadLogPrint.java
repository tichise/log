/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.log.android;

import android.os.Handler;
import android.os.Looper;

import net.mm2d.log.Log;
import net.mm2d.log.Log.Initializer;
import net.mm2d.log.Log.Print;

import androidx.annotation.NonNull;

/**
 * {@link Log}をAndroid上で使用するための出力クラスを提供する。
 *
 * <p>以下のようにすることで出力先をLogcatに変更することができる。
 *
 * <pre>{@code
 * Log.setPrint(MainThreadLogPrint.get());
 * }</pre>
 *
 * <p>{@link Log#initialize(boolean, boolean)}を使用する場合は、
 * {@link Log#setInitializer(Initializer)}で、
 * {@link AndroidLogInitializer#getMainThread()}を指定することで、
 * このクラスを利用した初期化が行われるようになる。
 *
 * <p>また、この出力では必ずメインスレッドから出力を行うため、
 * 複数スレッドからのリクエストが重複した場合でも、1リクエストずつ出力される。
 * メインスレッドの場合はそのまま出力し、
 * そうでない場合はメインスレッドのキューへ積む。
 * そのため、メインスレッドとそれ以外のスレッドでの出力順とコール順は必ずしも一致しない、
 * また、メインスレッドがロックされている間は出力が行われなくなる。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MainThreadLogPrint {
    /**
     * 唯一のインスタンスを返す。
     *
     * @return Printのインスタンス
     */
    @NonNull
    public static Print get() {
        return MainThreadLogPrintImpl.getInstance();
    }

    private static class MainThreadLogPrintImpl implements Print {
        @NonNull
        private static final MainThreadLogPrintImpl INSTANCE = new MainThreadLogPrintImpl();

        @NonNull
        static MainThreadLogPrintImpl getInstance() {
            return INSTANCE;
        }

        @NonNull
        private final Thread mMainThread = Looper.getMainLooper().getThread();
        @NonNull
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        @Override
        public void println(
                final int level,
                @NonNull final String tag,
                @NonNull final String message) {
            if (Thread.currentThread() == mMainThread) {
                printlnInner(level, tag, message);
                return;
            }
            mHandler.post(() -> printlnInner(level, tag, message));
        }

        private void printlnInner(
                final int level,
                @NonNull final String tag,
                @NonNull final String message) {
            final String[] lines = message.split("\n");
            for (final String line : lines) {
                android.util.Log.println(level, tag, line);
            }
        }
    }
}
