using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using System.Windows.Threading;
using mCubed.Classes;

namespace mCubed
{
	/// <summary>
	/// Interaction logic for mCubedSampler.xaml
	/// </summary>
	public partial class mCubedSampler : Window
	{
		public DispatcherTimer timer;
		public MediaFile FileTrack { get; set; }
		public bool MediaPlay { get; set; }
		public MediaElement Media { get { return (MediaElement)FindName("media"); } }

		public mCubedSampler()
		{
			timer = new DispatcherTimer();
			timer.Tick += new EventHandler(timerStop);
			InitializeComponent();
		}

		public mCubedSampler(MediaFile fileTrack)
			: this()
		{
			FileTrack = fileTrack;
			startTime.Text = "0:00";
			endTime.Text = this.FileTrack.MetaData.LengthString;
			length.Text = this.FileTrack.MetaData.LengthString;
			Media.Source = new Uri(this.FileTrack.MetaData.FilePath);
		}

		public void Play()
		{
			Stop();
			timer.Interval = TimeSpan.FromSeconds(double.Parse(this.playbackLength.Text));
			timer.Start();
			Media.Play();
			Media.Position = TimeSpan.FromSeconds(double.Parse(this.startTime.Text));
		}

		public void Stop()
		{
			timer.Stop();
			Media.Stop();
		}

		private void timerStop(object sender, EventArgs e)
		{
			Stop();
		}

		private void buttonMediaAction_Click(object sender, RoutedEventArgs e)
		{
			switch (((FrameworkElement)sender).Name)
			{
				case "buttonPlay": Play(); break;
				case "buttonStop": Stop(); break;
			}
		}

		private void playbackTime_TextChanged(object sender, TextChangedEventArgs e)
		{
			double st = (startTime.Text == "") ? 0 : double.Parse(startTime.Text);
			double et = (endTime.Text == "") ? 0 : double.Parse(endTime.Text);
			playbackLength.Text = (et - st).ToString();
		}
	}
}