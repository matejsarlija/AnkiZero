# Jetpack Compose Architecture Guide: State-Based vs Slot-Based APIs

## Overview

This guide provides architectural decision-making frameworks for choosing between state-based management and slot-based APIs in Jetpack Compose applications. As applications scale, the choice between these approaches becomes critical for maintainability, performance, and developer productivity.

## Table of Contents

1. [Architectural Patterns](#architectural-patterns)
2. [Tradeoff Analysis](#tradeoff-analysis)
3. [Scaling Evolution](#scaling-evolution)
4. [Decision Framework](#decision-framework)
5. [Implementation Strategies](#implementation-strategies)
6. [Performance Implications](#performance-implications)
7. [Best Practices](#best-practices)

## Architectural Patterns

### State-Based Management

State-based components manage their behavior through explicit state parameters or internal state management. These components provide focused, type-safe APIs with clear behavioral contracts.

**Characteristics:**
- Explicit state parameters
- Self-contained behavior logic
- Predictable API surface
- Strong compile-time validation

**Implementation Example:**
```kotlin
@Composable
fun ExpandableCard(
    title: String,
    content: String,
    initiallyExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    
    LaunchedEffect(isExpanded) {
        onExpandChange(isExpanded)
    }
    
    Card(
        modifier = modifier.clickable { 
            isExpanded = !isExpanded 
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            
            AnimatedVisibility(visible = isExpanded) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
```

### Slot-Based APIs

Slot-based components provide structural frameworks with composable lambda parameters, allowing clients to inject custom content into predefined positions.

**Characteristics:**
- Composable lambda parameters
- Flexible content injection
- Structural separation of concerns
- Runtime content determination

**Implementation Example:**
```kotlin
@Composable
fun FlexibleCard(
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
    actions: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            header?.let { headerContent ->
                headerContent()
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            content()
            
            actions?.let { actionContent ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    actionContent()
                }
            }
            
            footer?.let { footerContent ->
                Spacer(modifier = Modifier.height(8.dp))
                footerContent()
            }
        }
    }
}
```

## Tradeoff Analysis

### State-Based Management

#### Advantages

| Aspect | Benefit | Description |
|--------|---------|-------------|
| **API Simplicity** | Clear contracts | Well-defined parameters with explicit types |
| **Type Safety** | Compile-time validation | Errors caught during compilation rather than runtime |
| **Performance** | Predictable recomposition | Clear state boundaries enable optimization |
| **Testing** | Isolated behavior | Easy to test specific state transitions |
| **Self-Containment** | Encapsulated logic | Component handles its own behavioral requirements |

#### Disadvantages

| Aspect | Limitation | Description |
|--------|------------|-------------|
| **Flexibility** | Limited customization | Difficult to adapt beyond predefined use cases |
| **Coupling** | Logic-UI binding | Business logic tightly coupled to UI components |
| **Scalability** | Parameter proliferation | API surface grows with feature requirements |
| **Reusability** | Context specificity | Hard to repurpose for different scenarios |

### Slot-Based APIs

#### Advantages

| Aspect | Benefit | Description |
|--------|---------|-------------|
| **Flexibility** | Maximum customization | Clients provide arbitrary content for slots |
| **Composability** | Component combination | Easy integration with other components |
| **Reusability** | Context independence | Same component works across different use cases |
| **Separation** | Decoupled concerns | UI structure independent of content implementation |
| **Extensibility** | Future-proof design | API extensions without breaking changes |

#### Disadvantages

| Aspect | Limitation | Description |
|--------|------------|-------------|
| **Complexity** | Implementation overhead | More complex to design and maintain |
| **Runtime Errors** | Reduced type safety | Issues may only surface at runtime |
| **Performance** | Recomposition complexity | Harder to predict and optimize composition behavior |
| **Documentation** | Usage guidance | Requires extensive examples and documentation |

## Scaling Evolution

### Phase 1: Simple State-Based Components

**Characteristics:**
- Single-purpose components
- Limited customization needs
- Well-defined use cases
- Small development team

**Example:**
```kotlin
@Composable
fun LoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(text)
        }
    }
}
```

### Phase 2: Hybrid Approach

**Characteristics:**
- Common patterns identified
- Selective slot introduction
- Maintaining backward compatibility
- Growing team requirements

**Example:**
```kotlin
@Composable
fun ActionButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leadingIcon?.invoke()
            content()
        }
    }
}
```

### Phase 3: Full Slot-Based Architecture

**Characteristics:**
- Maximum flexibility requirements
- Complex component hierarchies
- Large, distributed team
- Multiple product contexts

**Example:**
```kotlin
@Composable
fun ComplexCard(
    modifier: Modifier = Modifier,
    header: (@Composable (CardScope.() -> Unit))? = null,
    content: @Composable CardScope.() -> Unit,
    actions: (@Composable (CardScope.() -> Unit))? = null,
    footer: (@Composable (CardScope.() -> Unit))? = null
) {
    val cardScope = remember { CardScopeImpl() }
    
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            header?.let { 
                cardScope.it()
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            cardScope.content()
            
            actions?.let {
                Spacer(modifier = Modifier.height(16.dp))
                cardScope.it()
            }
            
            footer?.let {
                Spacer(modifier = Modifier.height(12.dp))
                cardScope.it()
            }
        }
    }
}

interface CardScope {
    @Composable fun Divider()
    @Composable fun Section(content: @Composable () -> Unit)
}
```

## Decision Framework

### Selection Criteria Matrix

| Criteria | State-Based | Slot-Based | Evaluation Questions |
|----------|-------------|------------|---------------------|
| **Requirements Stability** | High | Low | Are component requirements well-defined and stable? |
| **Customization Needs** | Low | High | Do different use cases require significant customization? |
| **Team Experience** | Beginner-Friendly | Expert-Required | What is the team's Compose experience level? |
| **Performance Criticality** | High | Medium | Are there strict performance requirements? |
| **Reusability Requirements** | Low | High | Will the component be used across many contexts? |
| **API Complexity Tolerance** | Low | High | Can the team handle complex APIs? |

### Decision Tree

```
Start Here
    ↓
Are requirements well-defined and stable?
    ├─ Yes → Is performance critical?
    │           ├─ Yes → Use State-Based
    │           └─ No → Is customization needed?
    │                       ├─ No → Use State-Based
    │                       └─ Yes → Consider Hybrid
    └─ No → Is maximum flexibility required?
                ├─ Yes → Use Slot-Based
                └─ No → Will requirements evolve significantly?
                            ├─ Yes → Use Slot-Based
                            └─ No → Use Hybrid
```

## Implementation Strategies

### Layered Architecture Approach

**Foundation Layer: Slot-Based Primitives**
```kotlin
@Composable
fun BaseContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        content()
    }
}
```

**Abstraction Layer: Pattern Components**
```kotlin
@Composable
fun StandardCard(
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
    actions: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BaseContainer(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.headlineSmall
            ) {
                title()
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium
            ) {
                content()
            }
            
            actions?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    it()
                }
            }
        }
    }
}
```

**Application Layer: Domain-Specific Components**
```kotlin
@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardCard(
        title = { Text(product.name) },
        content = {
            Column {
                Text(
                    text = product.price,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = product.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (product.isFavorite) 
                        Icons.Filled.Favorite 
                    else 
                        Icons.Outlined.FavoriteBorder,
                    contentDescription = "Toggle Favorite"
                )
            }
            
            Button(onClick = onAddToCart) {
                Text("Add to Cart")
            }
        },
        modifier = modifier
    )
}
```

### Migration Strategy

#### Step 1: Identify Refactoring Candidates
```kotlin
// Evaluation criteria
data class ComponentAnalysis(
    val parameterCount: Int,
    val customizationRequests: Int,
    val useContexts: Int,
    val maintainabilityScore: Int
)

// Refactoring priority
fun calculateRefactoringPriority(analysis: ComponentAnalysis): Priority {
    return when {
        analysis.parameterCount > 8 -> Priority.HIGH
        analysis.customizationRequests > 5 -> Priority.HIGH
        analysis.useContexts > 3 -> Priority.MEDIUM
        else -> Priority.LOW
    }
}
```

#### Step 2: Implement Parallel APIs
```kotlin
// Maintain backward compatibility
@Composable
fun MyCard(
    title: String,
    content: String,
    showActions: Boolean = false,
    modifier: Modifier = Modifier
) {
    MyCard(
        title = { Text(title) },
        content = { Text(content) },
        actions = if (showActions) {
            { DefaultActions() }
        } else null,
        modifier = modifier
    )
}

@Composable
fun MyCard(
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
    actions: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // New slot-based implementation
}
```

#### Step 3: Gradual Migration
```kotlin
// Migration tracking
@Deprecated(
    message = "Use slot-based MyCard instead",
    replaceWith = ReplaceWith("MyCard(title = { Text(title) }, content = { Text(content) })")
)
@Composable
fun MyCard(title: String, content: String) { /* ... */ }
```

## Performance Implications

### State-Based Performance Characteristics

**Advantages:**
- Predictable recomposition boundaries
- Easier state hoisting optimization
- Clear dependency tracking
- Efficient `remember` usage patterns

**Example Optimization:**
```kotlin
@Composable
fun OptimizedStateCard(
    data: CardData,
    modifier: Modifier = Modifier
) {
    // Stable computation
    val processedData = remember(data.id, data.version) {
        data.expensiveProcessing()
    }
    
    // Stable callbacks
    val onClick = remember(data.id) {
        { /* handle click for data.id */ }
    }
    
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        // Render processedData
    }
}
```

### Slot-Based Performance Characteristics

**Challenges:**
- Broader recomposition scopes
- Complex state dependency tracking
- Lambda recreation overhead
- Unpredictable composition boundaries

**Optimization Strategies:**
```kotlin
@Composable
fun OptimizedSlotCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Minimize recomposition scope
    Card(modifier = modifier) {
        // Isolate content composition
        key("content-isolation") {
            content()
        }
    }
}

// Usage with stable lambdas
@Composable
fun ClientComponent() {
    val stableContent = remember {
        @Composable {
            Text("Stable content")
        }
    }
    
    OptimizedSlotCard(content = stableContent)
}
```

### Performance Measurement

```kotlin
// Performance monitoring
@Composable
fun PerformanceMonitoredComponent(
    content: @Composable () -> Unit
) {
    val compositionCount = remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        compositionCount.intValue++
        if (BuildConfig.DEBUG) {
            Log.d("Performance", "Component recomposed ${compositionCount.intValue} times")
        }
    }
    
    content()
}
```

## Best Practices

### API Design Guidelines

#### State-Based Components
```kotlin
@Composable
fun WellDesignedStateComponent(
    // Required parameters first
    data: ComponentData,
    
    // State parameters grouped
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    
    // Callbacks grouped
    onAction: () -> Unit = {},
    onStateChange: (ComponentState) -> Unit = {},
    
    // Styling parameters
    colors: ComponentColors = ComponentDefaults.colors(),
    
    // Modifier always last
    modifier: Modifier = Modifier
)
```

#### Slot-Based Components
```kotlin
@Composable
fun WellDesignedSlotComponent(
    // Modifier first for slot-based components
    modifier: Modifier = Modifier,
    
    // Required slots
    content: @Composable ComponentScope.() -> Unit,
    
    // Optional slots with clear naming
    header: (@Composable ComponentScope.() -> Unit)? = null,
    footer: (@Composable ComponentScope.() -> Unit)? = null,
    
    // Configuration parameters last
    configuration: ComponentConfiguration = ComponentConfiguration()
)
```

### Documentation Standards

```kotlin
/**
 * A flexible card component that provides customizable content slots.
 *
 * This component follows the slot-based API pattern, allowing maximum flexibility
 * for content customization while maintaining consistent visual structure.
 *
 * Example usage:
 * ```
 * FlexibleCard(
 *     header = {
 *         Text("Card Title", style = MaterialTheme.typography.headlineMedium)
 *     },
 *     content = {
 *         Column {
 *             Text("Main content goes here")
 *             Spacer(modifier = Modifier.height(8.dp))
 *             Text("Additional details")
 *         }
 *     },
 *     actions = {
 *         TextButton(onClick = { /* action */ }) {
 *             Text("Action")
 *         }
 *     }
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the card container
 * @param header Optional header content slot
 * @param content Main content slot - always visible
 * @param actions Optional actions slot, typically for buttons
 * @param footer Optional footer content slot
 */
@Composable
fun FlexibleCard(
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
    actions: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null
)
```

### Testing Strategies

#### State-Based Testing
```kotlin
@Test
fun stateBasedComponent_updatesCorrectly_whenStateChanges() {
    var currentState by mutableStateOf(ComponentState.Loading)
    
    composeTestRule.setContent {
        StateBasedComponent(
            state = currentState,
            onStateChange = { currentState = it }
        )
    }
    
    // Test initial state
    composeTestRule.onNodeWithText("Loading")
        .assertIsDisplayed()
    
    // Change state
    currentState = ComponentState.Success("Test Data")
    
    // Verify state change
    composeTestRule.onNodeWithText("Test Data")
        .assertIsDisplayed()
}
```

#### Slot-Based Testing
```kotlin
@Test
fun slotBasedComponent_rendersContent_inCorrectSlots() {
    composeTestRule.setContent {
        SlotBasedComponent(
            header = { Text("Header Content") },
            content = { Text("Main Content") },
            actions = { Button(onClick = {}) { Text("Action") } }
        )
    }
    
    // Verify all slots render
    composeTestRule.onNodeWithText("Header Content")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Main Content")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Action")
        .assertIsDisplayed()
}
```

## Conclusion

The choice between state-based management and slot-based APIs represents a fundamental architectural decision that impacts maintainability, performance, and developer experience. While state-based approaches offer simplicity and performance benefits for well-defined use cases, slot-based APIs provide the flexibility necessary for complex, evolving applications.

The key to successful Compose architecture lies in recognizing when each approach is appropriate and implementing a layered strategy that can evolve with application requirements. By following the guidelines and frameworks outlined in this document, development teams can make informed decisions that support both immediate needs and long-term scalability.

Regular architectural reviews and willingness to refactor components as requirements evolve will ensure that your Compose application remains maintainable and performant as it scales.