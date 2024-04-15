package com.example.project_gemini

object QuestionCategories {
    val FASHION = QuestionCategory(
        arrayOf(
            "Which fashion items are you most interested in? (Select all that apply)",
            "What influences your choice of clothing brands? (Select all that apply)",
            "What specific features are most important in your personal style? (Select all that apply)",
            "What type of personal grooming services interest you? (Select all that apply)",
            "Set a budget range for your fashion expenses. (Select one)"
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Clothing", "Shoes", "Accessories", "Jewelry", "Beauty Products")
                1 -> arrayOf("Brand Reputation", "Style Trends", "Quality", "Affordability", "Celebrity Endorsements")
                2 -> arrayOf("Comfort", "Trendiness", "Versatility", "Sustainability", "Customization")
                3 -> arrayOf("Hair Styling", "Skincare", "Makeup", "Personal Shopping", "Tailoring Services")
                4 -> arrayOf(
                    "Less than $50 per month",
                    "$50 - $100 per month",
                    "$100 - $200 per month",
                    "$200 - $300 per month",
                    "More than $300 per month"
                )
                else -> arrayOf()
            }
        }
    )

    val GENERAL = QuestionCategory(
        arrayOf(
            "What are your general interests? (Select all that apply)",
            "How do you prefer to spend your leisure time? (Select all that apply)",
            "What influences your general lifestyle choices? (Select all that apply)",
            "What type of personal development activities interest you? (Select all that apply)",
            "Set a budget range for your personal hobbies. (Select one)"
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Reading", "Gaming", "Sports", "Music", "Art")
                1 -> arrayOf("Outdoor Activities", "Indoor Activities", "Socializing", "Watching Movies/TV", "Traveling")
                2 -> arrayOf("Health and Wellness", "Environmental Consciousness", "Technology", "Cultural Influences", "Social Trends")
                3 -> arrayOf("Learning Courses", "Fitness Classes", "Meditation", "Networking Events", "Volunteering")
                4 -> arrayOf(
                    "Less than $50 per month",
                    "$50 - $100 per month",
                    "$100 - $200 per month",
                    "$200 - $300 per month",
                    "More than $300 per month"
                )
                else -> arrayOf()
            }
        }
    )

    val INSURANCE = QuestionCategory(
        arrayOf(
            "Which insurance products are you most interested in? (Select all that apply)",
            "What factors influence your choice of insurance provider? (Select all that apply)",
            "What specific coverage features are most important to you? (Select all that apply)",
            "What type of financial planning services are you interested in? (Select all that apply)",
            "Set a budget range for your insurance premiums. (Select one)"
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Health Insurance", "Life Insurance", "Property Insurance", "Auto Insurance", "Travel Insurance")
                1 -> arrayOf("Customer Service", "Coverage Options", "Pricing", "Claim Process", "Reputation")
                2 -> arrayOf("Comprehensive Coverage", "Liability Coverage", "Personal Injury Protection", "Property Damage Coverage", "Deductible Options")
                3 -> arrayOf("Retirement Planning", "Investment Planning", "Estate Planning", "Education Savings", "Wealth Management")
                4 -> arrayOf(
                    "Less than $50 per month",
                    "$50 - $100 per month",
                    "$100 - $200 per month",
                    "$200 - $300 per month",
                    "More than $300 per month"
                )
                else -> arrayOf()
            }
        }
    )

    val CULINARY = QuestionCategory(
        arrayOf(
            "Select your preferred spice and flavor profiles for meals.",
            "Choose your preferred types of flour and protein sources for meals.",
            "Indicate your preferences for sauces and sweetness levels in meals.",
            "Select your preferred beverage temperature.",
            "Choose your preferred fat sources in meals."
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Mild spice", "No spice", "Like ginger", "Like garlic", "Neutral on both ginger and garlic")
                1 -> arrayOf("Aata (whole wheat flour)", "Maida (all-purpose flour)", "Prefer tofu", "Prefer paneer (cow milk)", "Neutral/no preference")
                2 -> arrayOf("Red tomato sauce only", "No chili sauce", "Very sweet", "Moderate sweetness", "No preference")
                3 -> arrayOf("Very cold (with ice)", "Room temperature", "No preference")
                4 -> arrayOf("Love generous amounts of ghee/butter", "Prefer moderate amounts of ghee/butter", "Neutral on ghee/butter", "Prefer minimal or no ghee/butter", "No preference")
                else -> arrayOf()
            }
        }
    )


    val TRAVEL = QuestionCategory(
        arrayOf(
            "Which types of travel experiences interest you? (Select all that apply)",
            "What factors influence your choice of travel destinations? (Select all that apply)",
            "What specific activities do you enjoy during travel? (Select all that apply)",
            "What type of travel accommodations do you prefer? (Select all that apply)",
            "Set a budget range for your travel expenses. (Select one)"
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Adventure Travel", "Cultural Exploration", "Beach Destinations", "City Tours", "Nature and Wildlife")
                1 -> arrayOf("Cost", "Cultural Richness", "Adventure Opportunities", "Climate", "Safety")
                2 -> arrayOf("Hiking", "Shopping", "Culinary Exploration", "Photography", "Relaxation")
                3 -> arrayOf("Hotels", "Airbnb", "Hostels", "Camping", "Luxury Resorts")
                4 -> arrayOf(
                    "Less than $500 per trip",
                    "$500 - $1,000 per trip",
                    "$1,000 - $2,000 per trip",
                    "$2,000 - $3,000 per trip",
                    "More than $3,000 per trip"
                )
                else -> arrayOf()
            }
        }
    )

    val ALLERGY_CARD = QuestionCategory(
        arrayOf(
            "Do you have any allergies or dietary restrictions? (Select all that apply)",
            "What specific food allergies do you have?",
            "How severe are your allergies?",
            "Are there any specific cuisines or types of food you prefer due to allergies?",
            "Would you like restaurant recommendations based on allergy-friendly options?"
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Dairy", "Gluten", "Nuts", "Shellfish", "Soy")
                1 -> arrayOf("Dairy", "Gluten", "Nuts", "Shellfish", "Soy", "Other")
                2 -> arrayOf("Mild", "Moderate", "Severe", "Life-Threatening")
                3 -> arrayOf("Vegetarian", "Vegan", "Gluten-Free", "Nut-Free", "Other")
                4 -> arrayOf("Yes", "No")
                else -> arrayOf()
            }
        }
    )

    val FASHION_STYLE_PREFERENCE = QuestionCategory(
        arrayOf(
            "Which fashion styles do you prefer? (Select all that apply)",
            "What influences your choice of clothing brands and styles?",
            "What specific features are most important in your personal fashion style?",
            "Are there any specific colors or patterns you prefer?",
            "Set a budget range for your fashion expenses. (Select one)"
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Casual", "Formal", "Sporty", "Bohemian", "Classic")
                1 -> arrayOf("Celebrities", "Fashion Magazines", "Social Media", "Street Fashion", "Designer Brands")
                2 -> arrayOf("Comfort", "Trendiness", "Versatility", "Sustainability", "Customization")
                3 -> arrayOf("Neutral Colors", "Bold Patterns", "Earth Tones", "Monochrome", "Pastels")
                4 -> arrayOf(
                    "Less than $50 per month",
                    "$50 - $100 per month",
                    "$100 - $200 per month",
                    "$200 - $300 per month",
                    "More than $300 per month"
                )
                else -> arrayOf()
            }
        }
    )

    val PAYMENT_DATA_CARD = QuestionCategory(
        arrayOf(
            "What payment methods do you prefer to use? (Select all that apply)",
            "Do you use digital wallets or mobile payment apps?",
            "What factors influence your choice of payment methods?",
            "Are there any specific security features you look for in payment options?",
            "Set a budget range for your monthly expenses. (Select one)"
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Credit Card", "Debit Card", "Digital Wallets", "Bank Transfer", "Cash")
                1 -> arrayOf("Yes", "No")
                2 -> arrayOf("Rewards Program", "Security", "Convenience", "Low Fees", "Acceptance")
                3 -> arrayOf("Two-Factor Authentication", "Biometric Authentication", "Tokenization", "Encryption", "Contactless")
                4 -> arrayOf(
                    "Less than $500 per month",
                    "$500 - $1,000 per month",
                    "$1,000 - $2,000 per month",
                    "$2,000 - $3,000 per month",
                    "More than $3,000 per month"
                )
                else -> arrayOf()
            }
        }
    )

    val PURCHASE_HISTORY_CARD = QuestionCategory(
        arrayOf(
            "What types of products do you frequently purchase? (Select all that apply)",
            "Where do you usually shop for products?",
            "What influences your decision to make a purchase?",
            "Do you prefer online or offline shopping?",
            "Set a budget range for your monthly shopping expenses. (Select one)"
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Clothing", "Electronics", "Home Goods", "Beauty Products", "Groceries")
                1 -> arrayOf("Online Retailers", "Department Stores", "Local Shops", "Specialty Stores", "Marketplaces")
                2 -> arrayOf("Reviews and Ratings", "Brand Reputation", "Promotions and Discounts", "Product Quality", "Recommendations")
                3 -> arrayOf("Online", "Offline", "Both")
                4 -> arrayOf(
                    "Less than $100 per month",
                    "$100 - $200 per month",
                    "$200 - $300 per month",
                    "$300 - $400 per month",
                    "More than $400 per month"
                )
                else -> arrayOf()
            }
        }
    )

    val SOCIAL_MEDIA_CARD = QuestionCategory(
        arrayOf(
            "Which social media platforms do you use? (Select all that apply)",
            "How often do you engage with social media?",
            "What type of content do you most enjoy on social media?",
            "Do you follow influencers or celebrities on social media?",
            "Would you like personalized content recommendations based on your social media preferences?"
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Facebook", "Instagram", "Twitter", "LinkedIn", "TikTok")
                1 -> arrayOf("Multiple times a day", "Once a day", "A few times a week", "Rarely", "Never")
                2 -> arrayOf("Images and Videos", "News and Articles", "Memes", "Lifestyle Updates", "Educational Content")
                3 -> arrayOf("Yes", "No")
                4 -> arrayOf("Yes", "No")
                else -> arrayOf()
            }
        }
    )

    val HOTEL = QuestionCategory(
        arrayOf(
            "What is the purpose of your stay? (Business, leisure, celebration, etc.)",
            "Do you have a preferred room type or suite at the Four Seasons?",
            "What floor do you prefer to stay on? (Higher floors for views, lower floors for accessibility)",
            "Are there any specific views you prefer from your room? (Cityscape, ocean, garden)",
            "Will you require airport transfer services? If so, do you have a preference for the type of vehicle?",
            "Do you plan to hire a car during your stay or will you need transportation arranged for local travel?",
            "What type of mattress and pillow firmness do you prefer?",
            "Do you have any specific room setup requests? (Extra bedding, cribs for children, accessibility requirements)",
            "Are there any specific amenities you expect in your room? (Espresso machine, particular brands of toiletries)",
            "Do you have any dietary restrictions or allergies the hotel should be aware of?",
            "Would you like reservations at the hotel's restaurants? If so, what cuisines or dining experiences are you interested in?",
            "Do you prefer dining in private settings or exploring the hotel's various dining options?",
            "Are you interested in spa or wellness services during your stay? Any specific treatments?",
            "Do you have a regular fitness routine that you'd like to maintain while staying at the hotel? (Preferences for personal trainers, yoga, Pilates, etc.)",
            "Would you like information or reservations for local attractions and experiences?",
            "Are there any specific types of leisure activities you enjoy? (Golf, tennis, sailing)",
            "Do you require any special services during your stay? (Butler service, private chef, babysitting)",
            "Are you celebrating any special occasions during this stay? (Anniversaries, birthdays, business achievements)",
            "Do you have any specific privacy or security requests?",
            "Would you prefer a more discreet check-in/check-out process?"
        ),
        { questionIndex ->
            when (questionIndex) {
                0 -> arrayOf("Business", "Leisure", "Celebration", "Other")
                1 -> arrayOf("Standard Room", "Suite", "Deluxe Room", "Penthouse")
                2 -> arrayOf("Higher Floors", "Lower Floors", "Doesn't Matter")
                3 -> arrayOf("Cityscape", "Ocean View", "Garden View", "No Preference")
                4 -> arrayOf("Yes, Luxury Vehicle", "Yes, Standard Vehicle", "No, I'll Arrange My Own", "No, Not Needed")
                5 -> arrayOf("Yes, I'll Hire", "No, Arrange Local Transportation", "No, I'll Arrange My Own", "Not Needed")
                6 -> arrayOf("Soft", "Medium", "Firm", "No Preference")
                7 -> arrayOf("Extra Bedding", "Crib for Children", "Accessibility Features", "No Special Setup")
                8 -> arrayOf("Espresso Machine", "Luxury Toiletries", "Minibar", "No Specific Requests")
                9 -> arrayOf("Vegetarian", "Vegan", "Gluten-Free", "No Dietary Restrictions")
                10 -> arrayOf("Yes, Reservations Required", "No, Not Interested", "Open to Suggestions", "Other")
                11 -> arrayOf("Private Settings", "Exploring Various Options", "No Preference")
                12 -> arrayOf("Spa Treatments", "Wellness Packages", "Not Interested", "Other")
                13 -> arrayOf("Yes, Personal Trainer", "Yoga/Pilates Classes", "No Specific Routine", "Other")
                14 -> arrayOf("Yes, Information Needed", "No, Not Interested", "Open to Suggestions", "Other")
                15 -> arrayOf("Golf", "Tennis", "Sailing", "Other")
                16 -> arrayOf("Butler Service", "Private Chef", "Babysitting", "No Special Services Needed")
                17 -> arrayOf("Yes, Special Occasion", "No, Regular Stay", "Other")
                18 -> arrayOf("Privacy Requests", "Security Requests", "Both", "No Specific Requests")
                19 -> arrayOf("Yes, Discreet Process", "No, Standard Process", "Other")
                else -> arrayOf()
            }
        }
    )


}