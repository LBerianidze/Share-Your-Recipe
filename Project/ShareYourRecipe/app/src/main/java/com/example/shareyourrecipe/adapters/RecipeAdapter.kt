import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shareyourrecipe.R
import com.example.shareyourrecipe.model.Recipe
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class RecipeAdapter(private val onItemClick: (Recipe) -> Unit) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val recipeList = mutableListOf<Recipe>()

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeImage: ImageView = itemView.findViewById(R.id.imageRecipe)
        private val recipeName: TextView = itemView.findViewById(R.id.textRecipeName)
        private val recipeDuration: TextView = itemView.findViewById(R.id.textRecipeDuration)

        init {
            itemView.setOnClickListener {
                val food = recipeList[adapterPosition] // Retrieve the clicked food item
                onItemClick(food)
            }
        }

        fun bind(recipe: Recipe) {
            // Set the recipe data to the views
            Picasso.get().load(recipe.imageUrl).into(recipeImage)
            recipeName.text = recipe.name
            recipeDuration.text = "დრო: ${recipe.preparationTime}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val currentRecipe = recipeList[position]
        holder.bind(currentRecipe)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    fun setRecipes(recipes: List<Recipe>) {
        recipeList.clear()
        recipeList.addAll(recipes)
        notifyDataSetChanged()
    }
}
