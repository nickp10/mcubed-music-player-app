using System;
using System.Reflection;

namespace mCubed.Core {
	[AttributeUsage(AttributeTargets.Property, Inherited = false, AllowMultiple = false)]
	public sealed class MetaDataAttribute : Attribute {
		#region Properties

		public string ColumnAlias { get; set; }
		public string Description { get; set; }
		public string Display { get { return (ColumnAlias ?? DisplayFormula).ToReadableString(); } }
		public string DisplayFormula { get; set; }
		public string Formula { get; set; }
		public string Path { get; set; }
		public int Priority { get; set; }
		public PropertyInfo Property { get; set; }

		#endregion

		#region Constructor

		/// <summary>
		/// Create a new meta-data attribute with the given description
		/// </summary>
		/// <param name="description">The description for the attribute</param>
		public MetaDataAttribute(string description) {
			Description = description;
		}

		#endregion

		#region Members

		/// <summary>
		/// Retrieve a string representation of the meta-data attribute
		/// </summary>
		/// <returns>A string representation of the meta-data attribute</returns>
		public override string ToString() {
			return Description;
		}

		#endregion
	}
}